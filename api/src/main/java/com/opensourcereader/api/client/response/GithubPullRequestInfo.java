package com.opensourcereader.api.client.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPullRequestInfo(
    String url,
    @JsonProperty("html_url") String htmlUrl,
    @JsonProperty("diff_url") String diffUrl,
    @JsonProperty("patch_url") String patchUrl,
    @JsonProperty("merged_at") Instant mergedAt) {}
