package com.opensourcereader.api.dto;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepoFile;
import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;

public record OpenSourceRepoCreateResult(String path, RepoFileType type, Long id) {

  public static OpenSourceRepoCreateResult from(OpenSourceRepoFile opensourceRepoFile) {
    return new OpenSourceRepoCreateResult(
        opensourceRepoFile.getPath(),
        opensourceRepoFile.getRepoFileType(),
        opensourceRepoFile.getId());
  }
}
