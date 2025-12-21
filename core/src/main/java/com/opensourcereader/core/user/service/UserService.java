package com.opensourcereader.core.user.service;

import com.opensourcereader.core.security.dto.UserInfo;
import com.opensourcereader.core.user.dto.GithubUserCommand;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;

public interface UserService {

  User signup(UserSignUpCommand command);

  User signup(UserInfo userInfo);

  User guest(GithubUserCommand command);

  User findByProviderId(final Long providerId);

  User findByNickname(final String nickname);

  User findByEmail(final String email);
}
