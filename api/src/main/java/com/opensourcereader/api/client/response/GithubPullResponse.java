package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPullResponse(
    Long id,
    @JsonProperty("number") Integer tagId,
    String title,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    String state,
    @JsonProperty("comments") Integer commentCount,
    @JsonProperty("review_comments") Integer reviewCount,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt) {}
