package com.opensourcereader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import java.util.List;

public record OpensourceRepoCreateResponse(
    @JsonProperty(value = "tree") List<OpensourceRepoCreateResult> flatTrees
) {

  public static OpensourceRepoCreateResponse from(OpensourceRepo opensourceRepo) {
    List<OpensourceRepoCreateResult> opensourceRepoCreateResults = opensourceRepo.getContents()
        .stream()
        .map(OpensourceRepoCreateResult::from)
        .toList();
    return new OpensourceRepoCreateResponse(opensourceRepoCreateResults);
  }

}
