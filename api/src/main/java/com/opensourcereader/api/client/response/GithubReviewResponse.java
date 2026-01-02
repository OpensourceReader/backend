package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubReviewResponse(
    Long id,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    @JsonProperty("submitted_at") Instant submittedAt) {}
