package com.opensourcereader.api.dto;

import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.content.RepoEntryType;

public record OpenSourceRepoCreateResult(String path, RepoEntryType type, Long id) {

  public static OpenSourceRepoCreateResult from(OpenSourceRepoContent opensourceRepoContent) {
    return new OpenSourceRepoCreateResult(
        opensourceRepoContent.getPath(),
        opensourceRepoContent.getRepoEntryType(),
        opensourceRepoContent.getId());
  }
}
