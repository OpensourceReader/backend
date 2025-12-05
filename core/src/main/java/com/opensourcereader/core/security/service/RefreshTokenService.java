package com.opensourcereader.core.security.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.exception.OSRServerException;
import com.opensourcereader.core.security.entity.RefreshToken;
import com.opensourcereader.core.security.exception.TokenRefreshException;
import com.opensourcereader.core.security.repository.RefreshTokenRepository;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final UserRepository userRepository;

  private final RefreshTokenRepository refreshTokenRepository;

  // TODO core에 쓰일 application.yaml를 작성해야함
  //  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpireSeconds = 604800;

  public static final String REFRESH_TOKEN_NAME = "refresh_token";

  public RefreshToken createRefreshToken(String nickname) {
    User user =
        userRepository
            .findFirstByNickname(nickname)
            .orElseThrow(() -> new OSRServerException(HttpStatus.NOT_FOUND));

    Instant expiryDate = Instant.now().plusSeconds(refreshTokenExpireSeconds);

    RefreshToken refreshToken = RefreshToken.of(user, UUID.randomUUID().toString(), expiryDate);
    return refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException("Token Expired");
    }
    return token;
  }

  @Transactional
  public void invalidate(String nickname) {
    User user =
        userRepository
            .findFirstByNickname(nickname)
            .orElseThrow(() -> new OSRServerException(HttpStatus.NOT_FOUND));

    refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
  }

  @Transactional
  public void deleteToken(String token) {
    refreshTokenRepository.deleteRefreshTokenByToken(token);
  }
}
