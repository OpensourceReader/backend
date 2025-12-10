package com.opensourcereader.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public record OpenSourceRepoResponse(
    @JsonProperty(value = "tree") List<OpenSourceRepoCreateResult> flatTrees) {

  public static OpenSourceRepoResponse from(OpenSourceRepo opensourceRepo) {
    List<OpenSourceRepoCreateResult> openSourceRepoCreateResults =
        opensourceRepo.getContents().stream().map(OpenSourceRepoCreateResult::from).toList();
    return new OpenSourceRepoResponse(openSourceRepoCreateResults);
  }
}
