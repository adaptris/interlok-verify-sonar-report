package com.adaptris.verify.report.sonar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Issue {

  @Getter
  @Setter
  private String engineId;

  @Getter
  @Setter
  private String ruleId;

  @Getter
  @Setter
  private Severity severity;

  @Getter
  @Setter
  private Type type;

  @Getter
  @Setter
  private Location primaryLocation;
}
