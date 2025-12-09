package com.opensourcereader.core.user.service;

import com.opensourcereader.core.user.entity.User;

public interface UserService {

  User findByNickname(final String nickname);

  User findByEmail(final String email);
}
