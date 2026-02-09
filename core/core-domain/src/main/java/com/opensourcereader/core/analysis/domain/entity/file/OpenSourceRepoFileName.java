package com.opensourcereader.core.analysis.domain.entity.file;

import jakarta.persistence.Embeddable;

@Embeddable
public record OpenSourceRepoFileName(String name) {
  public static OpenSourceRepoFileName from(String path) {
    return new OpenSourceRepoFileName(extractName(path));
  }

  private static String extractName(String path) {
    String[] pathTokens = path.split("/");
    return pathTokens[pathTokens.length - 1];
  }
}
