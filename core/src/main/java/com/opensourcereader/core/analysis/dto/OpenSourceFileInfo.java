package com.opensourcereader.core.analysis.dto;

import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;

public record OpenSourceFileInfo(String path, RepoEntryType repoEntryType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, RepoEntryType.from(typeNumber), rawText);
  }
}
