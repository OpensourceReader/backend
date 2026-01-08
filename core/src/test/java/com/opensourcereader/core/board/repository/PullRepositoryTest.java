package com.opensourcereader.core.board.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.user.dto.UserSignUpCommand;
import com.opensourcereader.core.user.entity.User;
import org.junit.jupiter.api.Test;

@DataJpaTest
class PullRepositoryTest {

  @Autowired private PullRepository pullRepository;

  @Autowired private TestEntityManager entityManager;

  @Test
  void findAllByRepository() {
    User user = createUser();
    OpenSourceRepo repo = createRepo(user);
    entityManager.persist(user);
    entityManager.persist(repo);

    Pull pull1 = createPull(101, user, repo);
    Pull pull2 = createPull(102, user, repo);

    entityManager.persist(pull1);
    entityManager.persist(pull2);

    entityManager.flush();
    entityManager.clear();

    List<Integer> tagIds = pullRepository.findAllByRepository(repo);

    assertThat(tagIds).hasSize(2).containsExactlyInAnyOrder(101, 102);
  }

  private User createUser() {
    return User.from(new UserSignUpCommand("test@test.com", "pw123", "nick", "full", "userName"));
  }

  private OpenSourceRepo createRepo(User user) {
    return new OpenSourceRepo(user, "test-repo");
  }

  private Pull createPull(int number, User user, OpenSourceRepo repo) {
    Instant now = Instant.now();
    PullCommand command =
        PullCommand.builder()
            .id((long) number)
            .author(user)
            .repo(repo)
            .tagId(number)
            .createdAt(now)
            .updatedAt(now)
            .title("testPull")
            .body("testBody")
            .isOpened(true)
            .commentCount(1)
            .reviewCount(2)
            .build();
    return Pull.from(command);
  }
}
