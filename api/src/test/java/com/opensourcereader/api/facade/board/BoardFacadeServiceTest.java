package com.opensourcereader.api.facade.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import com.opensourcereader.api.controller.board.request.BoardGetRequest;
import com.opensourcereader.api.controller.board.response.BoardBaseResponse;
import com.opensourcereader.api.controller.board.response.BoardIssueResponse;
import com.opensourcereader.api.controller.board.response.BoardPreviewResponse;
import com.opensourcereader.api.controller.board.response.BoardPullResponse;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.service.*;
import com.opensourcereader.core.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardFacadeServiceTest {

  @Mock private IssueService issueService;
  @Mock private PullService pullService;
  @Mock private IssueCommentService issueCommentService;
  @Mock private PullCommentService pullCommentService;
  @Mock private LabelService labelService;
  @Mock private ReviewService reviewService;

  @InjectMocks private BoardFacadeService boardFacadeService;

  private static final Instant NOW = Instant.now();

  private User createDummyUser() {
    User user =
        User.builder()
            .username("testUser")
            .nickname("TestNick")
            .email("test@example.com")
            .password("password")
            .disabled(false)
            .build();

    ReflectionTestUtils.setField(user, "id", 1L);
    ReflectionTestUtils.setField(user, "createdAt", NOW);
    ReflectionTestUtils.setField(user, "updatedAt", NOW);

    return user;
  }

  private Issue createDummyIssue(Long id, User user) {
    Issue issue =
        Issue.builder()
            .title("Test Issue Title")
            .body("Test Issue Body")
            .user(user)
            .tagId(101L)
            .isOpened(true)
            .commentCount(5L)
            .build();

    ReflectionTestUtils.setField(issue, "id", id);
    ReflectionTestUtils.setField(issue, "createdAt", NOW);
    ReflectionTestUtils.setField(issue, "updatedAt", NOW);
    return issue;
  }

  private Pull createDummyPull(Long id, User user) {
    Pull pull =
        Pull.builder()
            .title("Test Pull Title")
            .body("Test Pull Body")
            .user(user)
            .tagId(202L)
            .isOpened(true)
            .reviewCount(2L)
            .commentCount(3L)
            .build();

    ReflectionTestUtils.setField(pull, "id", id);
    ReflectionTestUtils.setField(pull, "createdAt", NOW);
    ReflectionTestUtils.setField(pull, "updatedAt", NOW);
    return pull;
  }

  @Test
  @DisplayName("findAllByRepositoryId: 이슈와 PR을 모두 조회하여 미리보기 리스트로 반환한다.")
  void findAllPreviewByRepositoryId_Success() {
    // [Given]
    Long repoId = 100L;
    BoardGetRequest request = new BoardGetRequest(repoId);

    User user = createDummyUser();

    Issue issue = createDummyIssue(1L, user);
    Pull pull = createDummyPull(2L, user);

    Review review = Review.builder().pull(pull).user(user).build();
    ReflectionTestUtils.setField(review, "id", 10L);

    given(issueService.findAllByRepositoryId(repoId, true)).willReturn(List.of(issue));
    given(pullService.findAllByRepositoryId(repoId, true)).willReturn(List.of(pull));

    // [When]
    List<BoardPreviewResponse> result = boardFacadeService.findAllPreviewByRepositoryId(request);

    // [Then]
    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo("Test Issue Title");
    assertThat(result.get(1).title()).isEqualTo("Test Pull Title");
  }

  @Test
  @DisplayName("findByTagId: Issue가 존재할 경우 BoardIssueResponse를 반환한다.")
  void findByTagId_ReturnIssue() {
    // [Given]
    Long tagId = 123L;
    Long repoId = 100L;
    BoardGetRequest request = new BoardGetRequest(repoId);

    User user = createDummyUser();

    given(issueService.existedByTagId(repoId, tagId)).willReturn(true);
    given(pullService.existedByTagId(repoId, tagId)).willReturn(false);

    Issue issue = createDummyIssue(1L, user);
    ReflectionTestUtils.setField(issue, "tagId", tagId);

    given(issueService.findByTagId(repoId, tagId)).willReturn(issue);
    given(issueCommentService.findAllByIssueId(issue.getId())).willReturn(new ArrayList<>());

    // [When]
    BoardBaseResponse response = boardFacadeService.findByTagId(tagId, request);

    // [Then]
    assertThat(response).isInstanceOf(BoardIssueResponse.class);
    BoardIssueResponse issueRes = (BoardIssueResponse) response;
    assertThat(issueRes.getBoardAuthor().nickname()).isEqualTo("TestNick");
  }

  @Test
  @DisplayName("findByTagId: Pull이 존재할 경우 BoardPullResponse를 반환한다.")
  void findByTagId_ReturnPull() {
    // [Given]
    Long tagId = 456L;
    Long repoId = 100L;
    BoardGetRequest request = new BoardGetRequest(repoId);

    User user = createDummyUser();

    given(issueService.existedByTagId(repoId, tagId)).willReturn(false);
    given(pullService.existedByTagId(repoId, tagId)).willReturn(true);

    Pull pull = createDummyPull(2L, user);
    ReflectionTestUtils.setField(pull, "tagId", tagId);

    given(pullService.findByTagId(repoId, tagId)).willReturn(pull);
    given(reviewService.findAllByPullId(pull.getId())).willReturn(new ArrayList<>());

    // [When]
    BoardBaseResponse response = boardFacadeService.findByTagId(tagId, request);

    // [Then]
    assertThat(response).isInstanceOf(BoardPullResponse.class);
  }
}
