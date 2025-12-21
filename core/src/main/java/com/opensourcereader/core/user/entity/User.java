package com.opensourcereader.core.user.entity;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.dto.GithubUserCommand;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column(name = "provider_id")
  private Long providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "user_name")
  private String username;

  @Column(name = "login_name", nullable = false, unique = true)
  private String loginName;

  @Column(nullable = false)
  private String email;

  @Column private String password;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(nullable = false)
  private Boolean disabled = false;

  private User(String loginName, String email, String avatarUrl, Role role, String username) {
    this.loginName = loginName;
    this.email = email;
    this.password = null;
    this.avatarUrl = avatarUrl;
    this.role = role;
    this.username = username;
    this.disabled = false;
  }

  public static User newAdmin(UserSignUpCommand command) {
    return new User(
        command.loginName(), command.email(), command.avatarUrl(), Role.ADMIN, command.username());
  }

  public static User from(UserSignUpCommand command) {
    return new User(
        command.loginName(), command.email(), command.avatarUrl(), Role.USER, command.username());
  }

  public static User from(UserInfo userInfo) {
    return new User(
        userInfo.loginName(),
        userInfo.email(),
        userInfo.avatarUrl(),
        Role.OAUTH2,
        userInfo.username());
  }

  public static User newGuest(GithubUserCommand command) {
    return new User(command.loginName(), "fake@fake.com", command.avatarUrl(), Role.FAKE, "fake");
  }

  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }

  public void updateAvatar(String newAvatarUrl) {
    this.avatarUrl = updateField(this.avatarUrl, newAvatarUrl);
  }

  public void linkSocialProvider(Long newProviderId) {
    this.providerId = updateField(this.providerId, newProviderId);
  }

  public void updateRole(Role newRole) {
    if (!newRole.equals(Role.ADMIN)) {
      this.role = updateField(this.role, newRole);
    }
  }

  private <T> T updateField(T target, T replace) {
    if (target == null && replace != null) {
      return replace;
    }
    if (target != null && replace != null && !target.equals(replace)) {
      return replace;
    }
    return target;
  }
}
