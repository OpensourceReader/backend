package com.opensourcereader.core.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.opensourcereader.core.exception.OSRServerException;
import com.opensourcereader.core.security.dto.SignUpCommand;
import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("회원 가입 성공: Command")
  void signup_success() {
    // given
    String testNickname = "test";
    String testEmail = "test@github.no-com";
    SignUpCommand command = new SignUpCommand(testEmail, "password123", testNickname);

    given(userRepository.existsByNicknameAndEmail(anyString(), anyString())).willReturn(false);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class)))
        .willAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    // when
    User result = authService.signup(command);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNickname()).isEqualTo(testNickname);
    assertThat(result.getPassword()).isEqualTo("encodedPassword");

    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("회원 가입 실패: Command")
  void signup_fail() {
    // given
    String testNickname = "test";
    String testEmail = "test@github.no-com";
    SignUpCommand command = new SignUpCommand(testEmail, "password123", testNickname);

    given(userRepository.existsByNicknameAndEmail(anyString(), anyString())).willReturn(true);
    // when & then
    assertThatThrownBy(() -> authService.signup(command)).isInstanceOf(OSRServerException.class);
  }

  @Test
  @DisplayName("회원 가입 성공: UserInfo")
  void signup_success_with_info() {
    // given
    String testId = "12345";
    String testEmail = "test@github.no-com";
    String testName = "test";
    String testNickname = "test";
    String avatarUrl = "http://avatar.url";
    UserInfo userInfo = new UserInfo(testId, testEmail, testName, testNickname, avatarUrl);
    User user = User.of(testNickname, testEmail, UUID.randomUUID().toString()).build();

    given(userRepository.findUserByNicknameAndEmail(anyString(), anyString()))
        .willReturn(Optional.of(user));
    given(userRepository.save(any(User.class)))
        .willAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    // when
    User result = authService.signup(userInfo);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getNickname()).isEqualTo(testNickname);
    assertThat(result.getAvatarUrl()).isNotNull();
    assertThat(result.getProviderId()).isNotNull();

    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Oauth2 처리 메서드")
  void processOauth2UserTest() {
    // given
    Map<String, Object> attributes =
        Map.of(
            "id", "12345",
            "email", "github@test.com",
            "name", "test",
            "nickname", "testNick",
            "avatar_url", "http://avatar.url");
    User user = User.of("testNick", "github@test.com", "pw").providerId("12345").build();
    given(userRepository.findByProviderId("12345")).willReturn(Optional.of(user));
    // when
    User result = authService.processOAuth2User(attributes);
    // then
    assertThat(result.getProviderId()).isEqualTo("12345");
  }

  @Test
  @DisplayName("유저 정보 추출 : 성공")
  void extractHitHubUserInfo_success() {
    // given
    Map<String, Object> attributes =
        Map.of(
            "id", "12345",
            "email", "github@test.com",
            "name", "test",
            "nickname", "testNick",
            "avatar_url", "http://avatar.url");
    // when
    UserInfo result = authService.extractGitHubUserInfo(attributes);
    // then
    assertThat(result.providerId()).isEqualTo("12345");
    assertThat(result.email()).isEqualTo("github@test.com");
    assertThat(result.name()).isEqualTo("test");
    assertThat(result.avatarUrl()).isEqualTo("http://avatar.url");
  }

  @Test
  @DisplayName("GitHub API: 성공")
  void getGitHubEmail_Success() {
    // given
    String accessToken = "mock-token";

    List<Map<String, Object>> mockEmailResponse =
        List.of(
            Map.of("email", "secondary@test.com", "primary", false, "verified", true),
            Map.of("email", "primary@test.com", "primary", true, "verified", true), // 정답
            Map.of("email", "unverified@test.com", "primary", true, "verified", false));

    ResponseEntity<Object> responseEntity = new ResponseEntity<>(mockEmailResponse, HttpStatus.OK);

    given(
            restTemplate.exchange(
                eq("https://api.github.com/user/emails"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
        .willReturn(responseEntity);

    // when
    String resultEmail = authService.getGitHubEmail(accessToken);

    // then
    assertThat(resultEmail).isEqualTo("primary@test.com");
  }

  @Test
  @DisplayName("GitHub API: 실패")
  void getGitHubEmail_Fail_NoEmail() {
    // given
    String accessToken = "mock-token";

    List<Map<String, Object>> mockEmailResponse =
        List.of(Map.of("email", "secondary@test.com", "primary", false, "verified", true));

    ResponseEntity<Object> responseEntity = new ResponseEntity<>(mockEmailResponse, HttpStatus.OK);

    given(
            restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
        .willReturn(responseEntity);

    // when & then
    assertThatThrownBy(() -> authService.getGitHubEmail(accessToken))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("not found User Email IN GITHUB");
  }
}
