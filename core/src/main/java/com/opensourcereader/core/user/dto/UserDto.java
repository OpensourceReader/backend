package com.opensourcereader.core.user.dto;

import java.time.Instant;

import com.opensourcereader.core.user.entity.Role;
import com.opensourcereader.core.user.entity.User;

import lombok.Builder;

@Builder
public record UserDto(
    String nickname,
    String email,
    String avatarUrl,
    Role role,
    Instant createdAt,
    boolean disabled) {

  public static UserDto from(User user) {
    return UserDto.builder()
        .nickname(user.getLoginName())
        .email(user.getEmail())
        .avatarUrl(user.getAvatarUrl())
        .role(user.getRole())
        .createdAt(user.getCreatedAt())
        .disabled(user.getDisabled())
        .build();
  }
}
