package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPullCommentResponse(
    Long id,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt,
    @JsonProperty("diff_hunk") String diffHunk,
    String path,
    @JsonProperty("pull_request_review_id") Long reviewId) {}
