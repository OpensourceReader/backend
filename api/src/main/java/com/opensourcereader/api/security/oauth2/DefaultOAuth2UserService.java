package com.opensourcereader.api.security.oauth2;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.controller.auth.response.GitHubApiEmailResponse;
import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
  private final UserService userService;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> internalOAuth2UserService;
  private final GithubClient githubClient;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = internalOAuth2UserService.loadUser(userRequest);

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

  private User processOAuth2User(Map<String, Object> attributes) {
    UserInfo userInfo = extractGitHubUserInfo(attributes);

    try {
      return userService.findByProviderId(userInfo.providerId());
    } catch (Exception e) {
      return userService.signup(userInfo);
    }
  }

  private UserInfo extractGitHubUserInfo(Map<String, Object> attributes) {
    Long providerId = (Long) attributes.get("id");
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

  private String getGitHubEmail(String accessToken) {
    List<GitHubApiEmailResponse> emails = githubClient.fetchUserEmails(accessToken);

    if (emails != null) {
      return emails.stream()
          .filter(GitHubApiEmailResponse::primary)
          .filter(GitHubApiEmailResponse::verified)
          .findFirst()
          .map(GitHubApiEmailResponse::email)
          .orElseThrow(() -> new RuntimeException("not found User Email IN GITHUB"));
    }
    throw new RuntimeException("not found User Email IN GITHUB");
  }
}
