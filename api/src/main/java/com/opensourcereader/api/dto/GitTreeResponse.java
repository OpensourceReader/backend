package com.opensourcereader.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GitTreeResponse(
    String sha,
    String url,
    @JsonProperty(value = "tree")
    List<GitTreeDto> flatTrees,
    Boolean truncated
) {

}
