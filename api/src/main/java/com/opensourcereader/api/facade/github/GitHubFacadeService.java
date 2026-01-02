package com.opensourcereader.api.facade.github;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.client.GithubClient;
import com.opensourcereader.api.client.request.GithubIssueCommentRequest;
import com.opensourcereader.api.client.request.GithubRepoRequest;
import com.opensourcereader.api.client.response.GithubIssueCommentResponse;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullCommentResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.api.client.response.GithubRepoResponse;
import com.opensourcereader.api.client.response.GithubReviewResponse;
import com.opensourcereader.api.client.response.GithubUserResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.dto.PullCommentCommand;
import com.opensourcereader.core.board.dto.ReviewCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.service.IssueCommentService;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import com.opensourcereader.core.board.service.IssueSyncService;
import com.opensourcereader.core.board.service.PullCommentService;
import com.opensourcereader.core.board.service.ReviewService;
import com.opensourcereader.core.user.dto.GithubUserCommand;
import com.opensourcereader.core.user.entity.User;
import com.opensourcereader.core.user.service.UserSignUpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubFacadeService {
  // TODO 어드민 전용 서비스, 혹은 배치 전용 서비스가 될 예정

  private final GithubClient githubClient;

  private final GithubModelMapper modelMapper;

  private final OpenSourceRepoService openSourceRepoService;
  private final UserSignUpService userSignUpService;
  private final IssueSyncService issueSyncService;
  private final IssueRetrieveService issueRetrieveService;

  private final IssueCommentService issueCommentService;
  private final ReviewService reviewService;
  private final PullCommentService pullCommentService;

  public OpenSourceRepo createRepo(GithubRepoRequest request) {
    GithubRepoResponse response = githubClient.fetchRepo(request);
    User owner = findByUser(response.owner());
    return openSourceRepoService.getOrCreateRepoInDB(owner, response.name());
  }

  public boolean createIssues(GithubRepoRequest request) {
    OpenSourceRepo repo =
        openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());

    List<GithubIssueResponse> fetchedRepoIssues = githubClient.fetchRepoIssues(request);

    Queue<BoardBaseCommand> issueCommands = new ArrayDeque<>();
    Queue<BoardBaseCommand> pullCommands = new ArrayDeque<>();
    Queue<Integer> pullTagNumbers = new ArrayDeque<>();

    for (GithubIssueResponse fetched : fetchedRepoIssues) {
      User author = findByUser(fetched.user());
      Boolean isOpened = isOpened(fetched.state());
      BoardBaseCommand command;

      if (fetched.isPullRequest()) {
        pullTagNumbers.add(fetched.tagId());
      } else {
        command = modelMapper.toIssueCommand(fetched, author, repo, isOpened);
        issueCommands.add(command);
      }
    }

    List<GithubPullResponse> pullResponses = githubClient.fetchRepoPulls(request, pullTagNumbers);

    for (GithubPullResponse pullResponse : pullResponses) {
      User author = findByUser(pullResponse.user());
      Boolean isOpened = isOpened(pullResponse.state());
      PullCommand command = modelMapper.toPullCommand(pullResponse, author, repo, isOpened);
      pullCommands.add(command);
    }
    boolean issuesSuccess = issueSyncService.syncIssues(issueCommands);
    boolean pullSuccess = issueSyncService.syncIssues(pullCommands);

    return issuesSuccess && pullSuccess;
  }

  public boolean createIssueComments(GithubRepoRequest request) {
    List<IssueCommentCommand> commands = new ArrayList<>();

    List<Issue> entities =
        issueRetrieveService.findIssueOrPullWithComments(request.owner(), request.repoName());
    for (Issue issue : entities) {
      List<GithubIssueCommentResponse> responses =
          githubClient.fetchRepoIssueComments(
              new GithubIssueCommentRequest(request.owner(), request.repoName(), issue.getTagId()));

      for (GithubIssueCommentResponse response : responses) {
        User author = findByUser(response.user());
        IssueCommentCommand command = modelMapper.toIssueCommentCommand(response, author, issue);
        commands.add(command);
      }
    }
    return issueCommentService.saveIssueComments(commands);
  }

  public boolean createReviews(GithubIssueCommentRequest request) {
    OpenSourceRepo repo =
        openSourceRepoService.getRepoByOwnerNameAndTitle(request.owner(), request.repoName());

    Pull pull =
        issueRetrieveService.findIssueOrPullByTagId(repo.getId(), request.tagNumber(), Pull.class);

    List<ReviewCommand> commands = new ArrayList<>();
    List<GithubReviewResponse> responses = githubClient.fetchRepoReviews(request);

    for (GithubReviewResponse response : responses) {
      User author = findByUser(response.user());
      ReviewCommand command = modelMapper.toReviewCommand(response, author, pull);
      commands.add(command);
    }

    return reviewService.saveReviews(commands);
  }

  public boolean createPullComments(GithubIssueCommentRequest request) {
    List<PullCommentCommand> commands = new ArrayList<>();
    List<GithubPullCommentResponse> responses = githubClient.fetchRepoPullComments(request);

    for (GithubPullCommentResponse response : responses) {
      User author = findByUser(response.user());
      Review review = reviewService.findByProviderId(response.reviewId());
      PullCommentCommand command = modelMapper.toPullCommentCommand(response, author, review);
      commands.add(command);
    }

    return pullCommentService.savePullComments(commands);
  }

  private User findByUser(GithubUserResponse response) {
    GithubUserCommand command =
        new GithubUserCommand(response.id(), response.login(), response.avatarUrl());
    return userSignUpService.guest(command);
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
