package com.adaptris.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.adaptris.verify.report.sonar.Issue;
import com.adaptris.verify.report.sonar.Issues;
import com.adaptris.verify.report.sonar.Location;
import com.adaptris.verify.report.sonar.Severity;
import com.adaptris.verify.report.sonar.Type;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CreateVerifyReport {

  private final Options options;
  private final Options helpOnlyOptions;

  private static final String HELP_ARG = "help";
  private static final String REPORT_FILE_ARG = "reportFile";
  private static final String OUTPUT_FILE_ARG = "outputFile";
  private static final String ENGINE_ID_ARG = "engineId";
  private static final String RULE_ID_PREFIX_ARG = "ruleIdPrefix";
  private static final String LOCATION_FILE_PATH_ARG = "locationFilePath";

  private static final String ENGINE_ID_DEFAULT = "interlokVerify";
  private static final String RULE_ID_PREFIX_DEFAULT = "rule";
  private static final String LOCATION_FILE_PATH_DEFAULT = "./src/main/interlok/config/adapter.xml";
  private static final String REGEX_TYPES =
      Arrays.stream(Type.values()).map(Object::toString).collect(Collectors.joining("|"));
  private static final String REGEX_SEVERITIES =
      Arrays.stream(Severity.values()).map(Object::toString).collect(Collectors.joining("|"));

  private static final String REGEX_RULES = "^(?<type>" + REGEX_TYPES + "),(?<severity>"
      + REGEX_SEVERITIES + "),(?<message>(?<rule>[^:]+):.*)$";

  private static final String REGEX_MESSAGE_ONLY =
      "^(?<type>" + REGEX_TYPES + "),(?<severity>" + REGEX_SEVERITIES + "),(?<message>.*)$";
  
  private transient Pattern rulePattern;
  private transient Pattern messagePattern;
  
  CreateVerifyReport() {
    options = new Options();
    Option help = new Option("h", HELP_ARG, false, "Displays this..");
    options.addOption(help);
    options.addRequiredOption("f", REPORT_FILE_ARG, true, "(required) The verify report file");
    options.addRequiredOption("o", OUTPUT_FILE_ARG, true, "(required) The output file");
    options.addOption("e", ENGINE_ID_ARG, true, String.format("The engine id (default: %s)", ENGINE_ID_DEFAULT));
    options.addOption("r", RULE_ID_PREFIX_ARG, true, String.format("The rule id prefix (default: %s)", RULE_ID_PREFIX_DEFAULT));
    options.addOption("l", LOCATION_FILE_PATH_ARG, true, String.format("The location file path (default: %s)", LOCATION_FILE_PATH_DEFAULT));

    helpOnlyOptions = new Options();
    helpOnlyOptions.addOption(help);
    rulePattern = Pattern.compile(REGEX_RULES);
    messagePattern = Pattern.compile(REGEX_MESSAGE_ONLY);
  }

  public static void main(String[] args) throws Exception {
    CreateVerifyReport report = new CreateVerifyReport();
    report.run(args);
  }

  void run(String[] args) throws IOException, ParseException {
    ArgumentWrapper argumentWrapper = parseArguments(args);
    if (argumentWrapper != null) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      Issues issues = createIssues(argumentWrapper, readFile(argumentWrapper.getReportFile(), StandardCharsets.UTF_8));
      mapper.writerWithDefaultPrettyPrinter().writeValue(new File(argumentWrapper.getOutputFile()), issues);
    }
  }

  ArgumentWrapper parseArguments(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine helpCommandLine = parser.parse(helpOnlyOptions, args, true);
      if (helpCommandLine.hasOption(HELP_ARG)) {
        usage();
        return null;
      }
      CommandLine line = parser.parse(options, args);
      return new ArgumentWrapper(
        line.getOptionValue(ENGINE_ID_ARG, ENGINE_ID_DEFAULT),
        line.getOptionValue(RULE_ID_PREFIX_ARG, RULE_ID_PREFIX_DEFAULT),
        line.getOptionValue(LOCATION_FILE_PATH_ARG, LOCATION_FILE_PATH_DEFAULT),
        line.getOptionValue(REPORT_FILE_ARG),
        line.getOptionValue(OUTPUT_FILE_ARG)
      );
    } catch (ParseException e) {
      usage();
      throw e;
    }
  }

  Issues createIssues(ArgumentWrapper argumentWrapper, String report) {
    List<Issue> issueList = new ArrayList<>();
    try (Scanner scanner = new Scanner(report)) {
      int i = 1;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        createIssue(argumentWrapper, line, i++).ifPresent(issueList::add);
      }
    }
    return new Issues(issueList);
  }
  
  private Optional<Issue> createIssue(ArgumentWrapper argumentWrapper,String line, int count) {
    Matcher ruleMatcher = rulePattern.matcher(line);
    Matcher msgMatcher = messagePattern.matcher(line);
    if (ruleMatcher.matches()) {
      return Optional.of(new Issue(argumentWrapper.getEngineId(), ruleMatcher.group("rule").replace(" ", "_").toLowerCase(),
          Severity.valueOf(ruleMatcher.group("severity")),
          Type.valueOf(ruleMatcher.group("type")),
          new Location(
              ruleMatcher.group("message"),
              argumentWrapper.getLocationFilePath())));
    } else if (msgMatcher.matches()){
      return Optional.of(new Issue(argumentWrapper.getEngineId(), 
          argumentWrapper.getRuleIdPrefix() + count,
          Severity.valueOf(msgMatcher.group("severity")),
          Type.valueOf(msgMatcher.group("type")),
          new Location(
              msgMatcher.group("message"),
              argumentWrapper.getLocationFilePath())));
    }
    return Optional.empty();
  }

  private void usage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("interlok-verify-report", options);
  }

  private String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  @AllArgsConstructor
  static class ArgumentWrapper {

    @Getter
    private final String engineId;

    @Getter
    private final String ruleIdPrefix;

    @Getter
    private final String locationFilePath;

    @Getter
    private final String reportFile;

    @Getter
    private final String outputFile;
  }

}
