package com.opensourcereader.core.user.entity;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.Entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  private String providerId;

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
}
