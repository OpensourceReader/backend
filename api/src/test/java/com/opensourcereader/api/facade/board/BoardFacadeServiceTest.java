package com.opensourcereader.api.facade.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.opensourcereader.api.controller.board.request.BoardGetRequest;
import com.opensourcereader.api.controller.board.response.BoardPreviewResponse;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardFacadeServiceTest {
  private static final FixtureMonkey FIXTURE_MONKEY =
      FixtureMonkey.builder()
          .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
          .pushExactTypeArbitraryIntrospector(
              BoardGetRequest.class, ConstructorPropertiesArbitraryIntrospector.INSTANCE)
          .defaultNotNull(true)
          .build();

  @Mock private IssueRetrieveService issueRetrieveService;

  @InjectMocks private BoardFacadeService boardFacadeService;

  @Test
  @DisplayName("이슈와 PR을 모두 조회하여 미리보기 리스트로 반환한다")
  void givenRequest_thenSuccess() {
    // given
    BoardGetRequest request = FIXTURE_MONKEY.giveMeOne(BoardGetRequest.class);

    Issue issue = FIXTURE_MONKEY.giveMeOne(Issue.class);
    Pull pull = FIXTURE_MONKEY.giveMeOne(Pull.class);

    given(issueRetrieveService.findIssuesByRepositoryId(request.repositoryId()))
        .willReturn(List.of(issue, pull));

    // when
    List<BoardPreviewResponse> result = boardFacadeService.findAllPreviewByRepositoryId(request);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).title()).isEqualTo(issue.getTitle());
    assertThat(result.get(1).title()).isEqualTo(pull.getTitle());
  }
}
