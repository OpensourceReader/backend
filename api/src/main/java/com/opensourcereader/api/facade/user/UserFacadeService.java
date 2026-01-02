package com.opensourcereader.api.facade.user;

import com.opensourcereader.core.user.service.UserRetrieveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.api.controller.user.request.UserGetRequest;
import com.opensourcereader.core.user.dto.UserDto;
import com.opensourcereader.core.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserRetrieveService userRetrieveService;

  @Transactional(readOnly = true)
  public UserDto getUser(final UserGetRequest req) {
    User user = userRetrieveService.findByNickname(req.nickname());

    return UserDto.from(user);
  }
}
