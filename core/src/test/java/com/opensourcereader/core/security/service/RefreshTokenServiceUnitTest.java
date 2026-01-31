package com.opensourcereader.core.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.Optional;

import com.opensourcereader.core.security.entity.RefreshToken;
import com.opensourcereader.core.security.exception.TokenRefreshException;
import com.opensourcereader.core.security.repository.RefreshTokenRepository;
import com.opensourcereader.core.shared.exception.OSRServerException;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceUnitTest {

  @Mock private UserRepository userRepository;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks private RefreshTokenService refreshTokenService;

  @Test
  @DisplayName("토큰 생성 성공")
  void createRefreshToken_Success() {
    // Given
    String nickname = "testUser";
    User user = User.of(nickname, "test@email.com", "pw").build();

    given(userRepository.findFirstByNickname(nickname)).willReturn(Optional.of(user));
    given(refreshTokenRepository.save(any(RefreshToken.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // When
    RefreshToken result = refreshTokenService.createRefreshToken(nickname);

    // Then
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getToken()).isNotNull();
    assertThat(result.getExpiryDate()).isAfter(Instant.now());
    then(refreshTokenRepository).should().save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("토큰 생성 실패 (유저 없음)")
  void createRefreshToken_Fail_UserNotFound() {
    // Given
    given(userRepository.findFirstByNickname(anyString())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.createRefreshToken("unknown"))
        .isInstanceOf(OSRServerException.class);

    then(refreshTokenRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("만료 검증 성공")
  void verifyExpiration_Success() {
    // Given
    User user = User.of("user", "email", "pw").build();
    RefreshToken validToken = RefreshToken.of(user, "token", Instant.now().plusSeconds(3600));

    // When
    RefreshToken result = refreshTokenService.verifyExpiration(validToken);

    // Then
    assertThat(result).isEqualTo(validToken);
    then(refreshTokenRepository).should(never()).delete(any());
  }

  @Test
  @DisplayName("만료 검증 실패 (시간 초과)")
  void verifyExpiration_Expired() {
    // Given
    User user = User.of("user", "email", "pw").build();
    RefreshToken expiredToken = RefreshToken.of(user, "token", Instant.now().minusSeconds(3600));

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
        .isInstanceOf(TokenRefreshException.class)
        .hasMessage("Token Expired");

    then(refreshTokenRepository).should().delete(expiredToken);
  }

  @Test
  @DisplayName("토큰 무효화 성공")
  void invalidate_Success() {
    // Given
    String nickname = "user";
    User user = User.of(nickname, "email", "pw").build();
    RefreshToken token = RefreshToken.of(user, "token", Instant.now());

    given(userRepository.findFirstByNickname(nickname)).willReturn(Optional.of(user));
    given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(token));

    // When
    refreshTokenService.invalidate(nickname);

    // Then
    then(refreshTokenRepository).should().delete(token);
  }

  @Test
  @DisplayName("토큰 무효화 (토큰 없음)")
  void invalidate_UserExists_But_NoToken() {
    // Given
    String nickname = "user";
    User user = User.of(nickname, "email", "pw").build();

    given(userRepository.findFirstByNickname(nickname)).willReturn(Optional.of(user));
    given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

    // When
    refreshTokenService.invalidate(nickname);

    // Then
    then(refreshTokenRepository).should(never()).delete(any());
  }

  @Test
  @DisplayName("토큰 무효화 실패 (유저 없음)")
  void invalidate_Fail_UserNotFound() {
    // Given
    given(userRepository.findFirstByNickname(anyString())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> refreshTokenService.invalidate("unknown"))
        .isInstanceOf(OSRServerException.class);
  }

  @Test
  @DisplayName("토큰 문자열 삭제 성공")
  void deleteToken_Success() {
    // Given
    String tokenStr = "some-refresh-token";

    // When
    refreshTokenService.deleteToken(tokenStr);

    // Then
    then(refreshTokenRepository).should().deleteRefreshTokenByToken(tokenStr);
  }
}
