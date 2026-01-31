package com.opensourcereader.core.analysis.entity.content;

import jakarta.persistence.Embeddable;

@Embeddable
public record OpenSourceRepoContentName(String name) {
  public static OpenSourceRepoContentName from(String path) {
    return new OpenSourceRepoContentName(extractName(path));
  }

  private static String extractName(String path) {
    String[] pathTokens = path.split("/");
    return pathTokens[pathTokens.length - 1];
  }
}
