package com.opensourcereader.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;

public record GitTreeResponse(
    String sha,
    String url,
    @JsonProperty(value = "tree") List<GitTreeFileInfo> flatTrees,
    Boolean truncated
) {

}
