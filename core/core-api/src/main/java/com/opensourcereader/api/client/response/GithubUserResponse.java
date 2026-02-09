package com.opensourcereader.api.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserResponse(
    Long id, String login, @JsonProperty("avatar_url") String avatarUrl) {}
