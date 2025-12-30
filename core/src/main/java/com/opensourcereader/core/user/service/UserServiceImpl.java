package com.opensourcereader.core.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.dto.GithubUserCommand;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.Role;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.exception.UserNotFoundException;
import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public User signup(UserSignUpCommand command) {
    User user =
        userRepository
            .findFirstByLoginName(command.loginName())
            .orElseGet(() -> User.from(command));
    Role role = user.getRole();
    if (role.equals(Role.OAUTH2) || role.equals(Role.FAKE)) {
      String encode = passwordEncoder.encode(command.rawPassword());
      user.updatePassword(encode);
      user.updateRole(Role.USER);
    }
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public User signup(UserInfo userInfo) {
    User user =
        userRepository
            .findFirstByLoginName(userInfo.loginName())
            .orElseGet(() -> User.from(userInfo));

    user.updateAvatar(userInfo.avatarUrl());
    user.linkSocialProvider(userInfo.providerId());
    // 비밀번호가 없으면 비밀번호를 따로 집어넣어야 함
    if (user.getPassword() == null) {
      user.updatePassword(UUID.randomUUID().toString());
    }

    return userRepository.save(user);
  }

  @Override
  @Transactional
  public User guest(GithubUserCommand command) {
    User user =
        userRepository
            .findFirstByLoginName(command.loginName())
            .orElseGet(() -> User.newGuest(command));
    user.updateAvatar(command.avatarUrl());
    user.linkSocialProvider(command.providerId());
    if (user.getPassword() == null) {
      user.updatePassword(UUID.randomUUID().toString());
    }
    return userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByProviderId(Long providerId) {
    return userRepository.findByProviderId(providerId).orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByNickname(String loginName) {
    return userRepository.findFirstByLoginName(loginName).orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    return userRepository.findFirstByEmail(email).orElseThrow(UserNotFoundException::new);
  }
}
