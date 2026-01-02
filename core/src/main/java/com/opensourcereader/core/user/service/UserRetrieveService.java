package com.opensourcereader.core.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.exception.UserNotFoundException;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRetrieveService{

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public User findByProviderId(Long providerId) {
    return userRepository.findByProviderId(providerId).orElseThrow(UserNotFoundException::new);
  }

  @Transactional(readOnly = true)
  public User findByNickname(String loginName) {
    return userRepository.findFirstByLoginName(loginName).orElseThrow(UserNotFoundException::new);
  }

  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findFirstByEmail(email).orElseThrow(UserNotFoundException::new);
  }
}
