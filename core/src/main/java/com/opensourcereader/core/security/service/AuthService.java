package com.opensourcereader.core.security.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.opensourcereader.core.security.dto.SignUpCommand;
import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate = new RestTemplate();

  public User signup(final SignUpCommand command) {
    boolean existed = userRepository.existsByNicknameAndEmail(command.username(), command.email());
    if (existed) {
      return null;
    }
    String encode = passwordEncoder.encode(command.password());
    User user = User.of(command.username(), command.email(), encode).build();
    return userRepository.save(user);
  }

  public User processOAuth2User(Map<String, Object> attributes) {
    UserInfo userInfo = extractGitHubUserInfo(attributes);

    return userRepository
        .findByProviderId(userInfo.providerId())
        .orElseGet(() -> createNewUser(userInfo));
  }

  public UserInfo extractGitHubUserInfo(Map<String, Object> attributes) {
    String providerId = String.valueOf(attributes.get("id"));
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");
    String nickname = (String) attributes.get("login");
    String avatarUrl = (String) attributes.get("avatar_url");

    if (providerId == null) {
      throw new RuntimeException("MISSING OAUTH INFO");
    }

    if (email == null) {
      email = name + "@github.no-email";
    }

    return new UserInfo(providerId, email, name, nickname, avatarUrl);
  }

  public User createNewUser(UserInfo userInfo) {
    User user =
        User.of(userInfo.nickname(), userInfo.email(), UUID.randomUUID().toString())
            .avatarUrl(userInfo.avatarUrl())
            .providerId(userInfo.providerId())
            .build();

    return userRepository.save(user);
  }

  public String getGitHubEmail(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);

    HttpEntity<Object> entity = new HttpEntity<>(headers);

    ResponseEntity<Object> response =
        restTemplate.exchange(
            "https://api.github.com/user/emails",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<Object>() {});

    List<Map<String, Object>> emails = (List<Map<String, Object>>) response.getBody();

    if (emails != null) {
      return emails.stream()
          .filter(email -> Boolean.TRUE.equals(email.get("primary")))
          .filter(email -> Boolean.TRUE.equals(email.get("verified")))
          .findFirst()
          .map(email -> (String) email.get("email"))
          .orElse(null);
    }
    throw new RuntimeException("not found User Email IN GITHUB");
  }
}
