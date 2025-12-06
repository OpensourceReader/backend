package com.opensourcereader.core.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.opensourcereader.core.security.dto.SignUpCommand;
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
      return null;
    }
    String encode = passwordEncoder.encode(command.password());
    User user = User.of(command.username(), command.email(), encode).build();
    return userRepository.save(user);
  }
}
