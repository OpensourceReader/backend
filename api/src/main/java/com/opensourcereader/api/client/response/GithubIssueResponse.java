package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensourcereader.core.board.entity.State;

public record GithubIssueResponse(
    Long id,
    @JsonProperty("number") Integer tagId,
    String title,
    @JsonProperty("user") GithubUserResponse user,
    String body,
    State state,
    @JsonProperty("comments") Integer commentCount,
    @JsonProperty("created_at") Instant createdAt,
    @JsonProperty("updated_at") Instant updatedAt,
    @JsonProperty("pull_request") GithubPullRequestInfo pullRequest) {

  public boolean isPullRequest() {
    return pullRequest != null;
  }
}
