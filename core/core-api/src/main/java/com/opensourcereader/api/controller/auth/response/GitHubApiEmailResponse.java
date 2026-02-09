package com.opensourcereader.api.controller.auth.response;

public record GitHubApiEmailResponse(
    String email, boolean primary, boolean verified, String visibility) {}
