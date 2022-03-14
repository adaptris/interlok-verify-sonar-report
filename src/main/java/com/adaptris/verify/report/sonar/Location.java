package com.adaptris.verify.report.sonar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Location {

  @Getter
  @Setter
  private String message;

  @Getter
  @Setter
  private String filePath;
}
