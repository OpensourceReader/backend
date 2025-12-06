package com.opensourcereader.api.security.oauth2;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.security.service.AuthService;
import com.opensourcereader.core.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final AuthService authService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
        new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService();

    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
    if (attributes.get("email") == null) {
      String email = authService.getGitHubEmail(userRequest.getAccessToken().getTokenValue());
      if (email != null) {
        attributes.put("email", email);
      }
    }
    User savedUser = authService.processOAuth2User(attributes);

    UserConnection userConnection = new UserConnection(savedUser, Instant.now());

    return new OSRUser(userConnection, savedUser.getPassword(), attributes);
  }
}
