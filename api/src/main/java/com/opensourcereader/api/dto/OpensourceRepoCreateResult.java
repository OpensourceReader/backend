package com.opensourcereader.api.dto;

import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.entity.OpensourceRepoContent;

public record OpensourceRepoCreateResult(
    String path,
    ContentType type,
    Long id
) {

  public static OpensourceRepoCreateResult from(OpensourceRepoContent opensourceRepoContent) {
    return new OpensourceRepoCreateResult(
        opensourceRepoContent.getPath(),
        opensourceRepoContent.getContentType(),
        opensourceRepoContent.getId()
    );
  }

}
