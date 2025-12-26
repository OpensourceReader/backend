package com.opensourcereader.api.facade.github;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubRepoResponse;
import com.opensourcereader.api.client.response.GithubUserResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.service.IssueSyncService;
import com.opensourcereader.core.user.dto.GithubUserCommand;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.exception.UserNotFoundException;
import com.opensourcereader.core.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitHubFacadeService {
  // TODO 어드민 전용 서비스, 혹은 배치 전용 서비스가 될 예정

  private final GithubClient githubClient;

  private final GithubModelMapper modelMapper;

  private final OpenSourceRepoService openSourceRepoService;
  private final UserService userService;
  private final IssueSyncService issueSyncService;

  public OpenSourceRepo createRepo(GithubRepoRequest request) {
    try {
      return openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());
    } catch (OpenSourceRepoNotFoundException e) {
      GithubRepoResponse response = githubClient.fetchRepo(request);
      User owner = findByUser(response.owner());
      return openSourceRepoService.createRepoInDB(owner, response.name());
    }
  }

  public List<Issue> createIssues(GithubRepoRequest request) {
    OpenSourceRepo repo =
        openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());

    List<GithubIssueResponse> fetchedRepoIssues = githubClient.fetchRepoIssues(request);

    List<Issue> responses = new ArrayList<>();
    for (GithubIssueResponse fetched : fetchedRepoIssues) {
      User author = findByUser(fetched.user());
      Boolean isOpened = isOpened(fetched.state());
      BoardBaseCommand command;

      if (fetched.isPullRequest()) {
        command = modelMapper.toIssueCommand(fetched, author, repo, isOpened);
      } else {
        // TODO 일단 저장, 나중에 pull 전부 요청 때릴 때, 그때 Pull 객체 정보를 완전히 만들기
        command = modelMapper.toPullCommand(fetched, author, repo, isOpened);
      }
      issueSyncService.syncIssue(command);
    }

    return responses;
  }

  private User findByUser(GithubUserResponse response) {
    try {
      return userService.findByProviderId(response.id());
    } catch (UserNotFoundException e) {
      GithubUserCommand command =
          new GithubUserCommand(response.id(), response.login(), response.avatarUrl());
      return userService.guest(command);
    }
  }

  private Boolean isOpened(String state) {
    if (state.toLowerCase(Locale.ROOT).equals("open")) {
      return true;
    }
    if (state.toLowerCase(Locale.ROOT).equals("closed")) {
      return false;
    }
    return null;
  }
}
