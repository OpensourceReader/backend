package com.opensourcereader.api.security.oauth2;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.opensourcereader.api.security.UserConnection;
import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
        new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();

    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
    if (attributes.get("email") == null) {
      String email = getGitHubEmail(userRequest.getAccessToken().getTokenValue());
      if (email != null) {
        attributes.put("email", email);
      }
    }
    User savedUser = processOAuth2User(attributes);

    UserConnection userConnection = new UserConnection(savedUser, Instant.now());

    return new OSRUser(userConnection, savedUser.getPassword(), attributes);
  }

  private String getGitHubEmail(String accessToken) {
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

  private User processOAuth2User(Map<String, Object> attributes) {
    UserInfo userInfo = extractGitHubUserInfo(attributes);

    return userRepository
        .findByProviderId(userInfo.providerId)
        .orElseGet(() -> createNewUser(userInfo));
  }

  private UserInfo extractGitHubUserInfo(Map<String, Object> attributes) {
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

  private User createNewUser(UserInfo userInfo) {
    String username = userInfo.name;
    User user =
        User.of(
                username,
                userInfo.nickname,
                userInfo.email,
                UUID.randomUUID().toString(),
                userInfo.avatarUrl)
            .providerId(userInfo.providerId)
            .build();

    return userRepository.save(user);
  }

  private record UserInfo(
      String providerId, String email, String name, String nickname, String avatarUrl) {}
}
