package com.opensourcereader.core.user.service;

import java.util.Optional;

import com.opensourcereader.core.user.entity.User;

public interface UserService {

  Optional<User> findByNickname(final String nickname);

  Optional<User> findByEmail(final String email);
}
