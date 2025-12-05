package com.opensourcereader.api.facade.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.api.controller.user.request.UserGetRequest;
import com.opensourcereader.core.exception.OSRServerException;
import com.opensourcereader.core.user.dto.UserDto;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserService userService;

  @Transactional(readOnly = true)
  public UserDto getUser(final UserGetRequest req) {
    User user =
        userService
            .getUser(req.nickname())
            .orElseThrow(() -> new OSRServerException(HttpStatus.NOT_FOUND));

    return UserDto.of(user).build();
  }
}
