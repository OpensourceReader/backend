package com.opensourcereader.core.security.dto;

public record UserInfo(
    String providerId, String email, String username, String nickname, String avatarUrl) {}
