package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepoResponse(
    Long id,
    String name,
    @JsonProperty("owner") GithubUserResponse owner,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt) {}
