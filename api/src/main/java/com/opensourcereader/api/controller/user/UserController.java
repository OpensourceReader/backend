package com.opensourcereader.api.controller.user;

import com.opensourcereader.api.facade.user.UserFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserFacadeService userFacadeService;


}
