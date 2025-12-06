package com.opensourcereader.core.security.dto;

public record UserInfo(
    String providerId, String email, String name, String nickname, String avatarUrl) {}
