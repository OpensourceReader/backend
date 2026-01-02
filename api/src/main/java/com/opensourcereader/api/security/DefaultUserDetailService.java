package com.opensourcereader.api.security;

import com.opensourcereader.core.user.service.UserRetrieveService;
import java.time.Instant;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultUserDetailService implements UserDetailsService {

  private final UserRetrieveService userRetrieveService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    final User user = userRetrieveService.findByEmail(email);

    UserConnection userConnection = new UserConnection(user, Instant.now());

    return new OSRUser(userConnection, user.getPassword(), null);
  }
}
