package com.opensourcereader.api.security;

import java.time.Instant;

import com.opensourcereader.core.user.entity.Role;
import com.opensourcereader.core.user.entity.User;

import lombok.Getter;

@Getter
public class UserConnection {

  private final String nickname;

  private final String email;

  private final Role role;

  private final String providerUserId;

  private final Instant updatedAt;

  public UserConnection(User user, Instant updatedAt) {
    this.nickname = user.getNickname();
    this.email = user.getEmail();
    this.role = user.getRole();
    this.providerUserId = user.getProviderId();
    this.updatedAt = updatedAt;
  }
}
