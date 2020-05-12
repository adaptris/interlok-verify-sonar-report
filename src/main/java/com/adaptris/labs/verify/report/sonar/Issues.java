package com.adaptris.labs.verify.report.sonar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class Issues {

  @Getter
  @Setter
  private List<Issue> issues;
}
