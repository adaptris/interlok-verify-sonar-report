package com.adaptris.verify;

import com.adaptris.verify.report.Issue;
import com.adaptris.verify.report.Issues;
import com.adaptris.verify.report.Severity;
import com.adaptris.verify.report.Type;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateSonarReportTest {

  @Test
  void createIssues(){
    CreateSonarReport createSonarReport = new CreateSonarReport();
    Issues issues = createSonarReport.createIssues(
      new CreateSonarReport.ArgumentWrapper(
        "engineId",
        "rule",
        "./adapter.xml",
        "report.txt",
        "report.json"),
      "something\nsomething2");
    assertEquals(2, issues.getIssues().size());
    Issue issue1 = issues.getIssues().get(0);
    assertEquals("engineId", issue1.getEngineId());
    assertEquals("rule1", issue1.getRuleId());
    assertEquals(Type.CODE_SMELL, issue1.getType());
    assertEquals(Severity.INFO, issue1.getSeverity());
    assertEquals("./adapter.xml", issue1.getPrimaryLocation().getFilePath());
    assertEquals("something", issue1.getPrimaryLocation().getMessage());
    Issue issue2 = issues.getIssues().get(1);
    assertEquals("engineId", issue2.getEngineId());
    assertEquals("rule2", issue2.getRuleId());
    assertEquals(Type.CODE_SMELL, issue2.getType());
    assertEquals(Severity.INFO, issue2.getSeverity());
    assertEquals("./adapter.xml", issue2.getPrimaryLocation().getFilePath());
    assertEquals("something2", issue2.getPrimaryLocation().getMessage());
  }

  @Test
  void parseArgumentsShortHandDefaults() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("-f");
    args.add("./file.txt");
    args.add("-o");
    args.add("./out.json");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertEquals("./file.txt", argumentWrapper.getReportFile());
    assertEquals("./out.json", argumentWrapper.getOutputFile());
    assertEquals("interlokVerify", argumentWrapper.getEngineId());
    assertEquals("rule", argumentWrapper.getRuleIdPrefix());
    assertEquals("./src/main/interlok/config/adapter.xml", argumentWrapper.getLocationFilePath());
  }

  @Test
  void parseArgumentsDefaults() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--reportFile");
    args.add("./file.txt");
    args.add("--outputFile");
    args.add("./out.json");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertEquals("./file.txt", argumentWrapper.getReportFile());
    assertEquals("./out.json", argumentWrapper.getOutputFile());
    assertEquals("interlokVerify", argumentWrapper.getEngineId());
    assertEquals("rule", argumentWrapper.getRuleIdPrefix());
    assertEquals("./src/main/interlok/config/adapter.xml", argumentWrapper.getLocationFilePath());
  }

  @Test
  void parseArgumentsShortHandAll() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("-f");
    args.add("./file.txt");
    args.add("-o");
    args.add("./out.json");
    args.add("-e");
    args.add("thomas");
    args.add("-r");
    args.add("rulz");
    args.add("-l");
    args.add("./adapter.xml");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertEquals("./file.txt", argumentWrapper.getReportFile());
    assertEquals("./out.json", argumentWrapper.getOutputFile());
    assertEquals("thomas", argumentWrapper.getEngineId());
    assertEquals("rulz", argumentWrapper.getRuleIdPrefix());
    assertEquals("./adapter.xml", argumentWrapper.getLocationFilePath());
  }

  @Test
  void parseArgumentsAll() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--reportFile");
    args.add("./file.txt");
    args.add("--outputFile");
    args.add("./out.json");
    args.add("--engineId");
    args.add("thomas");
    args.add("-ruleIdPrefix");
    args.add("rulz");
    args.add("--locationFilePath");
    args.add("./adapter.xml");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertEquals("./file.txt", argumentWrapper.getReportFile());
    assertEquals("./out.json", argumentWrapper.getOutputFile());
    assertEquals("thomas", argumentWrapper.getEngineId());
    assertEquals("rulz", argumentWrapper.getRuleIdPrefix());
    assertEquals("./adapter.xml", argumentWrapper.getLocationFilePath());
  }

  @Test
  void parseArgumentsShortHandHelp() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("-h");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertNull(argumentWrapper);
  }

  @Test
  void parseArgumentsHelp() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--help");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertNull(argumentWrapper);
  }

  @Test
  void parseArgumentsHelpWithOtherArguments() throws Exception {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--help");
    args.add("--reportFile");
    args.add("./file.txt");
    args.add("--outputFile");
    args.add("./out.json");
    CreateSonarReport.ArgumentWrapper argumentWrapper = createSonarReport.parseArguments(args.toArray(new String[]{}));
    assertNull(argumentWrapper);
  }


  @Test()
  void parseArgumentsMissingReport() {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--outputFile");
    args.add("./out.json");
    assertThrows(MissingOptionException.class, () -> {
      createSonarReport.parseArguments(args.toArray(new String[]{}));
    });
  }

  @Test
  void parseArgumentsMissingOutput() {
    CreateSonarReport createSonarReport = new CreateSonarReport();
    List<String> args = new ArrayList<>();
    args.add("--reportFile");
    args.add("./report.txt");
    assertThrows(MissingOptionException.class, () -> {
      createSonarReport.parseArguments(args.toArray(new String[]{}));
    });
  }

  @Test
  void main() throws Exception {
    File tmpDir = createTempDirectory();
    File outputFile = new File(tmpDir, "out.json");
    File reportFile = new File(tmpDir, "report.txt");
    FileUtils.writeStringToFile(reportFile, "something\nsomething else", StandardCharsets.UTF_8);
    List<String> args = new ArrayList<>();
    args.add("--reportFile");
    args.add(reportFile.getAbsolutePath());
    args.add("--outputFile");
    args.add(outputFile.getAbsolutePath());
    CreateSonarReport.main(args.toArray(new String[]{}));
    assertTrue(outputFile.exists());
    ObjectMapper mapper = new ObjectMapper();
    Issues issues = mapper.readValue(outputFile, Issues.class);
    assertEquals(2, issues.getIssues().size());
    Issue issue1 = issues.getIssues().get(0);
    assertEquals("interlokVerify", issue1.getEngineId());
    assertEquals("rule1", issue1.getRuleId());
    assertEquals(Type.CODE_SMELL, issue1.getType());
    assertEquals(Severity.INFO, issue1.getSeverity());
    assertEquals("./src/main/interlok/config/adapter.xml", issue1.getPrimaryLocation().getFilePath());
    assertEquals("something", issue1.getPrimaryLocation().getMessage());
    Issue issue2 = issues.getIssues().get(1);
    assertEquals("interlokVerify", issue2.getEngineId());
    assertEquals("rule2", issue2.getRuleId());
    assertEquals(Type.CODE_SMELL, issue2.getType());
    assertEquals(Severity.INFO, issue2.getSeverity());
    assertEquals("./src/main/interlok/config/adapter.xml", issue2.getPrimaryLocation().getFilePath());
    assertEquals("something else", issue2.getPrimaryLocation().getMessage());
    cleanUpTempDirectory(tmpDir);
  }

  @Test
  void mainInvalidArguments() throws Exception {
    List<String> args = new ArrayList<>();
    args.add("--reportFile");
    args.add("./report.txt");
    assertThrows(MissingOptionException.class, () -> {
      CreateSonarReport.main(args.toArray(new String[]{}));
    });
  }

  @Test
  void mainHelpWithOtherArguments() throws Exception {
    File tmpDir = createTempDirectory();
    File outputFile = new File(tmpDir, "out.json");
    File reportFile = new File(tmpDir, "report.txt");
    FileUtils.writeStringToFile(reportFile, "something\nsomething else", StandardCharsets.UTF_8);
    List<String> args = new ArrayList<>();
    args.add("--help");
    args.add("--reportFile");
    args.add(reportFile.getAbsolutePath());
    args.add("--outputFile");
    args.add(outputFile.getAbsolutePath());
    CreateSonarReport.main(args.toArray(new String[]{}));
    assertFalse(outputFile.exists());
    cleanUpTempDirectory(tmpDir);
  }

  private File createTempDirectory() throws IOException {
    File tempDir = File.createTempFile(CreateSonarReportTest.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists()) {
      tempDir.mkdir();
    }
    return tempDir;
  }

  public void cleanUpTempDirectory(File tempDir) {
    for (final File f : tempDir.listFiles()) {
      f.delete();
    }
    tempDir.delete();
  }



}
