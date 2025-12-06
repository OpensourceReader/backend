package com.opensourcereader.core.analysis.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpensourceRepoContentName {

  private String name;

  public static OpensourceRepoContentName from(String path) {
    return new OpensourceRepoContentName(extractName(path));
  }

  private OpensourceRepoContentName(String name) {
    this.name = name;
  }

  private static String extractName(String path) {
    String[] pathTokens = path.split("/");
    return pathTokens[pathTokens.length - 1];
  }

}
