package com.opensourcereader.api.dto;

import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;

public record OpenSourceRepoCreateResult(String path, ContentType type, Long id) {

  public static OpenSourceRepoCreateResult from(OpenSourceRepoContent opensourceRepoContent) {
    return new OpenSourceRepoCreateResult(
        opensourceRepoContent.getPath(),
        opensourceRepoContent.getContentType(),
        opensourceRepoContent.getId());
  }
}
