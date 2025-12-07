package com.opensourcereader.core.analysis.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContentName {

  private String name;

  public static OpenSourceRepoContentName from(String path) {
    return new OpenSourceRepoContentName(extractName(path));
  }

  private OpenSourceRepoContentName(String name) {
    this.name = name;
  }

  private static String extractName(String path) {
    String[] pathTokens = path.split("/");
    return pathTokens[pathTokens.length - 1];
  }

}
