package com.opensourcereader.core.user.dto;

import java.time.temporal.ChronoUnit;

import com.opensourcereader.core.user.entity.Role;
import com.opensourcereader.core.user.entity.User;

import lombok.Builder;

@Builder
public record UserDto(
    String nickname,
    String email,
    String avatarUrl,
    Role role,
    String createdAt,
    boolean disabled) {

  public static UserDtoBuilder of(User user) {
    // time format : yyyy-MM-ddTHH:mm:ss.sssZ
    String createdAtToString = user.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).toString();

    return UserDto.builder()
        .nickname(user.getNickname())
        .email(user.getEmail())
        .role(user.getRole())
        .createdAt(createdAtToString)
        .disabled(user.getDisabled());
  }
}
