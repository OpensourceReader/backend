package com.opensourcereader.api.security.login;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.opensourcereader.core.security.dto.UserConnection;

import lombok.Getter;

@Getter
public class OSRUser implements UserDetails, OAuth2User {

  private final UserConnection userConnection;

  private final String password;

  private final Map<String, Object> attributes;

  public OSRUser(UserConnection userConnection, String password, Map<String, Object> attributes) {
    this.userConnection = userConnection;
    this.password = password;
    this.attributes = attributes;
  }

  @Override
  public String getName() {
    return userConnection.getEmail();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE" + userConnection.getRole()));
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return userConnection.getNickname();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }
}
