package com.opensourcereader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import java.util.List;

public record GitTreeResponse(
    String sha,
    String url,
    @JsonProperty(value = "tree")
    List<GitTreeFileInfo> flatTrees,
    Boolean truncated
) {

}
