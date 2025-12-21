package com.opensourcereader.api.facade.github;

import com.opensourcereader.api.client.response.GithubRepoResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.api.client.response.GithubUserResponse;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.service.IssueService;
import com.opensourcereader.core.board.service.PullService;
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
  private final IssueService issueService;
  private final PullService pullService;

  public OpenSourceRepo createRepo(GithubRepoRequest request) {
    try {
      return openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(),
          request.repoName());
    } catch (OpenSourceRepoNotFoundException e) {
      GithubRepoResponse response = githubClient.fetchRepo(request);
      User owner = findByUser(response.owner());
      return openSourceRepoService.createRepoInDB(owner, response.name());
    }
  }

  public List<Issue> createIssues(GithubRepoRequest request) {
    OpenSourceRepo repo = openSourceRepoService.getRepoByOwnerNameAndTitle(
        request.owner(), request.repoName());

    List<GithubIssueResponse> fetchedRepoIssues = githubClient.fetchRepoIssues(request);

    List<Issue> responses = new ArrayList<>();
    for (GithubIssueResponse fetched : fetchedRepoIssues) {
      User author = findByUser(fetched.user());

      Boolean isOpened = isOpened(fetched.state());

      BoardBaseCommand command = modelMapper.toCommand(fetched, author, repo, isOpened);

      Issue issue = issueService.create(command);
      responses.add(issue);
    }

    return responses;
  }

  public List<Pull> createPulls(GithubRepoRequest request) {
    OpenSourceRepo repo = openSourceRepoService.getRepoByOwnerNameAndTitle(
        request.owner(), request.repoName());

    List<GithubPullResponse> fetchedRepoPulls = githubClient.fetchRepoPulls(request);

    List<Pull> responses = new ArrayList<>();
    for (GithubPullResponse fetched : fetchedRepoPulls) {
      User author = findByUser(fetched.user());

      Boolean isOpened = isOpened(fetched.state());

      PullCommand command = modelMapper.toCommand(fetched, author, repo, isOpened);

      Pull pull = pullService.create(command);
      responses.add(pull);
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
