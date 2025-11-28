package com.opensourcereader.api.facade.user;

import com.opensourcereader.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

  private final UserService userService;

}
