package com.opensourcereader.core.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class UserServiceSpringTest {

  @Autowired private UserRetrieveService userRetrieveService;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("유저 저장 후 조회")
  void test() {
    // given
    String encode = passwordEncoder.encode("test123");
    UserSignUpCommand command = UserSignUpCommand.of("test@github.no-email", "test123", "test");
    User user = User.from(command);
    user.updatePassword(encode);
    userRepository.save(user);
    // when
    User result = userRetrieveService.findByEmail("test@github.no-email");
    // then
    assertThat(result).isNotNull();
  }
}
