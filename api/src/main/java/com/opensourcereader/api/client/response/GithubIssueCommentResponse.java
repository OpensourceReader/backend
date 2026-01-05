package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubIssueCommentResponse(
    Long id,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt) {}
