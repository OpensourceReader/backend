package com.opensourcereader.api.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.opensourcereader.core.user.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockRestServiceServer
@Transactional
@ActiveProfiles("test")
public class DefaultOauth2UserServiceTest {

  @Autowired
  private DefaultOAuth2UserService defaultOAuth2UserService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MockRestServiceServer mockServer;

  @MockitoBean(name = "internalOAuth2UserService")
  private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

  @Test
  @DisplayName("통합 테스트: 이메일 없는 유저가 로그인 시 API를 통해 이메일 요청을 한다.")
  void loadUser_Integration_CallGitHubApi(){
    // given
    String fakeToken = "accessToken";
    OAuth2UserRequest oAuth2UserRequest = mockUserRequest(fakeToken);

    Map<String, Object> attributes = Map.of(
        "id", 12345,
        "login", "nickname",
        "username", "Kim User"
    );

    DefaultOAuth2User auth2User = new DefaultOAuth2User(
        Collections.emptyList(),
        attributes,
        "login"
    );

    String gitHubApiResponseJson = """
        [
          {
            "email" : "primary@email.com",
            "primary" : true,
            "verified" : true,
            "visibility" : "public"
          }
        ]
        """;

    given(delegate.loadUser(any())).willReturn(auth2User);

    mockServer.expect(requestTo("https://api.github.com/user/emails"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(gitHubApiResponseJson, MediaType.APPLICATION_JSON));
    // when
    OAuth2User result = defaultOAuth2UserService.loadUser(oAuth2UserRequest);

    // then
    assertThat(result.getAttributes().get("email")).isEqualTo("primary@email.com");

    boolean existsed = userRepository.existsByNicknameAndEmail("nickname", "primary@email.com");
    assertThat(existsed).isTrue();

    mockServer.verify();
  }

  private OAuth2UserRequest mockUserRequest(String fakeToken) {
    return new OAuth2UserRequest(
        ClientRegistration.withRegistrationId("github")
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://localhost:8080/login/oauth2/code/github")
            .authorizationUri("https://github.com/login/oauth/authorize")
            .tokenUri("https://github.com/login/oauth/access_token")
            .userInfoUri("https://api.github.com/user")
            .userNameAttributeName("id")
            .build(),
        new OAuth2AccessToken(TokenType.BEARER, fakeToken, null, null));
  }


}
