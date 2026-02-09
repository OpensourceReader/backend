package com.opensourcereader.api.security.login;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensourcereader.api.controller.auth.request.LoginRequest;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("h2")
public class JsonLoginFilterIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  private static final String LOGIN_URL = "/api/v1/auth/login";

  @BeforeEach
  void setUp() {
    String encodedPassword = passwordEncoder.encode("password123");
    UserSignUpCommand command = UserSignUpCommand.of("test@email.com", "testuser", encodedPassword);
    User user = User.from(command);
    user.updatePassword(encodedPassword);
    userRepository.save(user);
  }

  @Test
  @DisplayName("로그인 성공: 올바른 JSON으로 요청 시 인증에 성공 (200 OK)")
  void attemptAuthentication_Success() throws Exception {
    // given
    LoginRequest loginRequest = new LoginRequest("test@email.com", "password123");
    String requestBody = objectMapper.writeValueAsString(loginRequest);

    // when & then
    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("로그인 실패: 비밀번호가 틀리면 401 오류")
  void attemptAuthentication_Fail_WrongPassword() throws Exception {
    // given
    LoginRequest loginRequest = new LoginRequest("test@email.com", "wrongPassword");
    String requestBody = objectMapper.writeValueAsString(loginRequest);

    // when & then
    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(requestBody))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("로그인 실패: JSON 형식이 잘못되면 400 오류")
  void attemptAuthentication_Fail_InvalidJson() throws Exception {
    // given
    String brokenJson = "{ \"email\": \"test@email.com\", \"password\": ";

    // when & then
    mockMvc
        .perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(brokenJson))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }
}
