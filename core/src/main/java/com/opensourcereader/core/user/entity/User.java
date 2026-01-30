package com.opensourcereader.core.user.entity;

import com.opensourcereader.core.shared.BaseEntity;
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

  private String providerId;

  @Enumerated(EnumType.STRING)
  private Role role;

  private String username;

  private String nickname;

  private String email;

  private String password;

  private String avatarUrl;

  private Boolean disabled;

  public static UserBuilder of(String nickname, String email, String password) {

    return User.builder().nickname(nickname).email(email).password(password).disabled(false);
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
