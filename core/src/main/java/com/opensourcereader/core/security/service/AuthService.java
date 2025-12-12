package com.opensourcereader.core.security.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.opensourcereader.core.exception.OSRServerException;
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

  public User signup(final SignUpCommand command) {
    boolean existed = userRepository.existsByNicknameAndEmail(command.username(), command.email());
    if (existed) {
      throw new OSRServerException(HttpStatus.BAD_REQUEST);
    }
    String encode = passwordEncoder.encode(command.password());
    User user = User.of(command.username(), command.email(), encode).build();
    return userRepository.save(user);
  }

  public User signup(UserInfo userInfo) {
    User user =
        userRepository
            .findUserByNicknameAndEmail(userInfo.nickname(), userInfo.email())
            .orElseGet(
                () ->
                    User.of(userInfo.nickname(), userInfo.email(), UUID.randomUUID().toString())
                        .build());
    user.updateAvatar(userInfo.avatarUrl());
    user.linkSocialProvider(userInfo.providerId());
    return userRepository.save(user);
  }
}
