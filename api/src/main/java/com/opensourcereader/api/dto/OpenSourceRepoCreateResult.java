package com.opensourcereader.api.dto;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepoFile;
import com.opensourcereader.core.analysis.domain.entity.file.RepoEntryType;

public record OpenSourceRepoCreateResult(String path, RepoEntryType type, Long id) {

  public static OpenSourceRepoCreateResult from(OpenSourceRepoFile opensourceRepoFile) {
    return new OpenSourceRepoCreateResult(
        opensourceRepoFile.getPath(),
        opensourceRepoFile.getRepoEntryType(),
        opensourceRepoFile.getId());
  }
}
