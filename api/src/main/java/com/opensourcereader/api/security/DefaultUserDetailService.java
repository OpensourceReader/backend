package com.opensourcereader.api.security;

import java.time.Instant;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.security.login.OSRUser;
import com.opensourcereader.core.security.dto.UserConnection;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.exception.UserNotFoundException;
import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultUserDetailService implements UserDetailsService {

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
    final User user =
        userService.findByNickname(nickname).orElseThrow(() -> new UserNotFoundException(nickname));

    UserConnection userConnection = new UserConnection(user, Instant.now());

    return new OSRUser(userConnection, user.getPassword(), null);
  }
}
