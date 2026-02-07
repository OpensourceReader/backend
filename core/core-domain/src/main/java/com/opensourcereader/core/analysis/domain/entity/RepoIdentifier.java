package com.opensourcereader.core.analysis.domain.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public record RepoIdentifier(String ownerName, String repoName) {

  public static RepoIdentifier of(String cloneUrl) {
    return new RepoIdentifier(extractOwnerName(cloneUrl), extractRepoName(cloneUrl));
  }

  private static String extractOwnerName(String cloneUrl) {
    String[] parts = normalize(cloneUrl).split("/");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid repository URL: " + cloneUrl);
    }
    return parts[parts.length - 2];
  }

  private static String extractRepoName(String cloneUrl) {
    String[] parts = normalize(cloneUrl).split("/");
    if (parts.length < 1) {
      throw new IllegalArgumentException("Invalid repository URL: " + cloneUrl);
    }
    return parts[parts.length - 1];
  }

  private static String normalize(String cloneUrl) {
    String cleaned =
        cloneUrl.replace(".git", "").replace("git@", "").replace("github.com:", "github.com/");

    if (cleaned.endsWith("/")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }

    return cleaned;
  }
}
