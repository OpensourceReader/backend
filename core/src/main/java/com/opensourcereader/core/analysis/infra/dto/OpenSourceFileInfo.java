package com.opensourcereader.core.analysis.infra.dto;

import com.opensourcereader.core.analysis.entity.content.RepoEntryType;

public record OpenSourceFileInfo(String path, RepoEntryType repoEntryType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, RepoEntryType.from(typeNumber), rawText);
  }
}
