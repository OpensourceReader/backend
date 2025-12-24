package com.opensourcereader.core.analysis.entity;

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

  public static String getExtension(String name) {
    int lastIndexOfZero = name.lastIndexOf('.') + 1;
    return name.substring(lastIndexOfZero);
  }

  public static boolean isNotJavaFile(String path) {
    int lastIndexOfZero = path.lastIndexOf('.') + 1;
    return !path.substring(lastIndexOfZero).equals("java");
  }
}
