package com.opensourcereader.core.user.dto;

public record UserSignUpCommand(
    String email, String rawPassword, String loginName, String avatarUrl, String username) {

  public static UserSignUpCommand of(String email, String rawPassword, String loginName) {
    return new UserSignUpCommand(email, rawPassword, loginName, null, null);
  }
}
