package com.opensourcereader.api.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GithubRepoResponse(
    Long id,
    String name,
    @JsonProperty("owner") GithubUserResponse owner,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt) {}
