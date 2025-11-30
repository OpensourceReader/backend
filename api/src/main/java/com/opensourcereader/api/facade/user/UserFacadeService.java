package com.opensourcereader.api.facade.user;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserService userService;
}
