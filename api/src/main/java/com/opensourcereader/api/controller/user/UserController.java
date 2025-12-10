package com.opensourcereader.api.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.controller.user.request.UserGetRequest;
import com.opensourcereader.api.facade.user.UserFacadeService;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserFacadeService userFacadeService;

  @GetMapping
  public ResponseEntity<UserDto> getUser(@RequestBody UserGetRequest req) {
    final UserDto response = userFacadeService.getUser(req);

    return ResponseEntity.ok(response);
  }
}
