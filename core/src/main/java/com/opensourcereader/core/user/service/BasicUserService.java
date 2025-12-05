package com.opensourcereader.core.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;

  @Override
  public Optional<User> getUser(String nickname) {
    return userRepository.findFirstByNickname(nickname);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userRepository.findFirstByEmail(email);
  }
}
