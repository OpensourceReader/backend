package com.opensourcereader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import java.util.List;

public record OpenSourceRepoCreateResponse(
    @JsonProperty(value = "tree") List<OpenSourceRepoCreateResult> flatTrees
) {

  public static OpenSourceRepoCreateResponse from(OpenSourceRepo opensourceRepo) {
    List<OpenSourceRepoCreateResult> openSourceRepoCreateResults = opensourceRepo.getContents()
        .stream()
        .map(OpenSourceRepoCreateResult::from)
        .toList();
    return new OpenSourceRepoCreateResponse(openSourceRepoCreateResults);
  }

}
