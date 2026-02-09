package com.opensourcereader.api.client.request;

public record GithubIssueCommentRequest(String owner, String repoName, Integer tagNumber) {}
