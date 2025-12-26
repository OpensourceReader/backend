package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubIssueResponse(
    Long id,
    @JsonProperty("number") Long tagId,
    String title,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    String state,
    @JsonProperty("comments") Long commentCount,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt,
    @JsonProperty("pull_request") GithubPullRequestInfo pullRequest) {

  public boolean isPullRequest() {
    return pullRequest != null;
  }
}
