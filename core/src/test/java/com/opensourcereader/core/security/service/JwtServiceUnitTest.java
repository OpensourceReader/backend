package com.opensourcereader.core.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Date;

import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JwtServiceUnitTest {
  private JwtService jwtService;

  // 테스트용 비밀키와 만료 시간
  private static final String TEST_SECRET_KEY = "testing";
  private static final long TEST_EXPIRATION_SECONDS = 3600L; // 1시간

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();

    ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);
    ReflectionTestUtils.setField(jwtService, "accessTokenExpireSeconds", TEST_EXPIRATION_SECONDS);

    jwtService.init();
  }

  @Test
  @DisplayName("토큰 생성 및 복호화 성공: 닉네임 추출 성공")
  void createAndDecodeToken_Success() {
    // given
    String nickname = "testUser";
    String email = "test@example.com";

    // when
    String token = jwtService.encoder(nickname, email);
    String decodedNickname = jwtService.decode(token);

    // then
    assertThat(token).isNotNull();
    assertThat(decodedNickname).isEqualTo(nickname);
  }

  @Test
  @DisplayName("토큰 검증 성공: 유효한 토큰")
  void validateJwt_Success() {
    // given
    String token = jwtService.encoder("user", "email@test.com");

    // when
    DecodedJWT result = jwtService.validateJwt(token);

    // then
    assertThat(result.getSubject()).isEqualTo("email@test.com");
    assertThat(result.getClaim("username").asString()).isEqualTo("user");
  }

  @Test
  @DisplayName("토큰 검증 실패: 만료된 토큰")
  void validateJwt_Expired() {
    // given
    Instant pastTime = Instant.now().minusSeconds(100);
    String expiredToken =
        JWT.create()
            .withSubject("expired@test.com")
            .withExpiresAt(Date.from(pastTime))
            .sign(Algorithm.HMAC256(TEST_SECRET_KEY));

    // when & then
    assertThatThrownBy(() -> jwtService.validateJwt(expiredToken))
        .isInstanceOf(JWTVerificationException.class)
        .hasMessage("Token Expired");
  }

  @Test
  @DisplayName("토큰 검증 실패: 서명이 다른 토큰")
  void validateJwt_InvalidSignature() {
    // given:
    String fakeSecretKey = "wrong-secret-key";
    String forgedToken =
        JWT.create()
            .withSubject("hacker@test.com")
            .withClaim("username", "hacker")
            .sign(Algorithm.HMAC256(fakeSecretKey));

    // when & then
    assertThatThrownBy(() -> jwtService.validateJwt(forgedToken))
        .isInstanceOf(JWTVerificationException.class)
        .hasMessage("Invalid Signature");
  }

  @Test
  @DisplayName("토큰 검증 실패: 형식이 잘못된 토큰")
  void validateJwt_Malformed() {
    // given
    String garbageToken = "not-valid-token";

    // when & then
    assertThatThrownBy(() -> jwtService.validateJwt(garbageToken))
        .isInstanceOf(JWTVerificationException.class);
  }
}
