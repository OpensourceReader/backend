package com.opensourcereader.core.security.dto;

public record UserInfo(
    Long providerId, String email, String username, String loginName, String avatarUrl) {}
