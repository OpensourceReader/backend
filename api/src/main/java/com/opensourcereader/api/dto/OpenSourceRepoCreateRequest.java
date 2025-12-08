package com.opensourcereader.api.dto;

public record OpenSourceRepoCreateRequest(
    String openSourceUri, String repoReference) {}
