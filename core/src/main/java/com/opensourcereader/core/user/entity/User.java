package com.opensourcereader.core.user.entity;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column private String providerId;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column private String username;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column private String avatarUrl;

  @Column(nullable = false)
  private Boolean disabled;

  public static UserBuilder of(String nickname, String email, String password) {

    return User.builder()
        .role(Role.USER)
        .nickname(nickname)
        .email(email)
        .password(password)
        .disabled(false);
  }

  public void updateAvatar(String newAvatarUrl) {
    this.avatarUrl = updateField(this.avatarUrl, newAvatarUrl);
  }

  public void linkSocialProvider(String newProviderId) {
    this.providerId = updateField(this.providerId, newProviderId);
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
