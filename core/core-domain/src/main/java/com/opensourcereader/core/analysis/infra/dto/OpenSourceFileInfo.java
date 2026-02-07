package com.opensourcereader.core.analysis.infra.dto;

import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;

public record OpenSourceFileInfo(String path, RepoFileType repoFileType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, RepoFileType.from(typeNumber), rawText);
  }
}
