package com.opensourcereader.core.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.opensourcereader.core.shared.exception.OSRServerException;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public User findByNickname(String nickname) {
    return userRepository
        .findFirstByNickname(nickname)
        .orElseThrow(() -> new OSRServerException(HttpStatus.NOT_FOUND));
  }

  @Override
  public User findByEmail(String email) {
    return userRepository
        .findFirstByEmail(email)
        .orElseThrow(() -> new OSRServerException(HttpStatus.NOT_FOUND));
  }
}
