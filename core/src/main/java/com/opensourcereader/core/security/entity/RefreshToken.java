package com.opensourcereader.core.security.entity;

import java.time.Instant;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {
  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  private String token;

  private Instant expiryDate;

  public static RefreshToken of(User user, String token, Instant expiryDate) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.user = user;
    refreshToken.token = token;
    refreshToken.expiryDate = expiryDate;
    return refreshToken;
  }
}
