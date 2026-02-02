package com.opensourcereader.core.analysis.infra.dto;

import com.opensourcereader.core.analysis.domain.entity.file.RepoEntryType;

public record OpenSourceFileInfo(String path, RepoEntryType repoEntryType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, RepoEntryType.from(typeNumber), rawText);
  }
}
