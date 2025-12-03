package com.opensourcereader.core.user.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.UpdateTimestamp;

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
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreatedDate private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  private String providerId;

  private Role role;

  private String username;

  private String nickname;

  private String email;

  private String password;

  private String avatarUrl;

  private Boolean disabled;

  public static UserBuilder of(
      String username, String nickname, String email, String password, String avatarUrl) {

    return User.builder()
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .username(username)
        .nickname(nickname)
        .email(email)
        .password(password)
        .avatarUrl(avatarUrl)
        .disabled(false);
  }
}
