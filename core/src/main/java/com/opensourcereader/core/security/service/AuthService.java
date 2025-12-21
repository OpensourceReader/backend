package com.opensourcereader.core.security.service;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
}
