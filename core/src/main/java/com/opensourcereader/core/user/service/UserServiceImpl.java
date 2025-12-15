package com.opensourcereader.core.user.service;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.exception.UserNotFoundException;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public User findByProviderId(String providerId) {
    return userRepository.findByProviderId(providerId).orElseThrow(UserNotFoundException::new);
  }

  @Override
  public User findByNickname(String nickname) {
    return userRepository.findFirstByNickname(nickname).orElseThrow(UserNotFoundException::new);
  }

  @Override
  public User findByEmail(String email) {
    return userRepository.findFirstByEmail(email).orElseThrow(UserNotFoundException::new);
  }
}
