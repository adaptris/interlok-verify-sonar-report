# interlok-verify-report
[![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-verify-report.svg)](https://github.com/adaptris/interlok-verify-report/tags) [![Build Status](https://travis-ci.org/adaptris/interlok-verify-report.svg?branch=develop)](https://travis-ci.org/adaptris/interlok-verify-report) [![CircleCI](https://circleci.com/gh/adaptris/interlok-verify-report/tree/develop.svg?style=svg)](https://circleci.com/gh/adaptris/interlok-verify-report/tree/develop) [![Actions Status](https://github.com/adaptris/interlok-verify-report/workflows/Java%20CI/badge.svg)](https://github.com/adaptris/interlok-verify-report/actions) [![codecov](https://codecov.io/gh/adaptris/interlok-verify-report/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-verify-report) [![Total alerts](https://img.shields.io/lgtm/alerts/g/adaptris/interlok-verify-report.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok-verify-report/alerts/) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/adaptris/interlok-verify-report.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/adaptris/interlok-verify-report/context:java)

Simple command line application that generates sonars generic issue format based on interlokVerify output.

interlokVerify in the [interlok-build-parent][interlok-build-parent] uses `-configtest` to check the configuration unmarshals. A service will also log a warning if the service has been deprecated. Using log4j2.xml filtering it creates a report.

Example report.txt:

```
[PayloadFromMetadataService(set-payload)] is a payload-from-metadata-service; use payload-from-template or metadata-to-payload instead.
```

This application will take that report and create JSON report that can be imported into sonar.

This can be tested in isolation using this project:

```
.\gradlew run --args="--reportFile ./build/report.txt --outputFile ./build/out.json"
```

The report generation has also been added to the [interlok-build-parent][interlok-build-parent], which exposes a property `interlokVerifySonarReport` which means you can use it with the sonar plugin:

```
sonarqube {
  properties {
    property "sonar.projectKey", "adaptris_interlok-hello-world"
    property "sonar.organization", "adaptris"
    property "sonar.host.url", "https://sonarcloud.io"
    property "sonar.sourceEncoding", "UTF-8"
    property "sonar.sources", "./src/main/interlok"
    property "sonar.tests", "./src/test/interlok"
    property "sonar.externalIssuesReportPaths", interlokVerifySonarReport
  }
}
```

A working example can be found at [interlok-hello-world][interlok-hello-world]

[interlok-build-parent]: https://github.com/adaptris/interlok-build-parent
[interlok-hello-world]: https://github.com/adaptris/interlok-hello-world
