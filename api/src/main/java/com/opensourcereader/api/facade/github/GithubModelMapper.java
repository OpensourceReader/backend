package com.opensourcereader.api.facade.github;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.client.response.GithubIssueCommentResponse;
import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullCommentResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.api.client.response.GithubReviewResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.dto.PullCommentCommand;
import com.opensourcereader.core.board.dto.ReviewCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.user.entity.User;

@Component
public class GithubModelMapper {
  public BoardBaseCommand toIssueCommand(
      GithubIssueResponse response, User author, OpenSourceRepo repo) {
    return BoardBaseCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .tagId(response.tagId())
        .author(author)
        .repo(repo)
        .title(response.title())
        .body(response.body())
        .state(response.state())
        .commentCount(response.commentCount())
        .build();
  }

  public PullCommand toPullCommand(GithubPullResponse response, User author, OpenSourceRepo repo) {
    return PullCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .tagId(response.tagId())
        .author(author)
        .repo(repo)
        .title(response.title())
        .body(response.body())
        .state(response.state())
        .commentCount(response.commentCount())
        .reviewCount(response.reviewCount())
        .build();
  }

  public PullCommand toPullCommand(GithubIssueResponse response, User author, OpenSourceRepo repo) {
    return PullCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .tagId(response.tagId())
        .author(author)
        .repo(repo)
        .title(response.title())
        .body(response.body())
        .state(response.state())
        .commentCount(response.commentCount())
        .build();
  }

  public PullCommand toPullCommand(BoardBaseCommand command, Integer reviewCommentCounts) {
    return PullCommand.builder()
        .id(command.getId())
        .createdAt(command.getCreatedAt())
        .updatedAt(command.getUpdatedAt())
        .tagId(command.getTagId())
        .author(command.getAuthor())
        .repo(command.getRepo())
        .title(command.getTitle())
        .body(command.getBody())
        .state(command.getState())
        .commentCount(command.getCommentCount())
        .reviewCount(reviewCommentCounts)
        .build();
  }

  public IssueCommentCommand toIssueCommentCommand(
      GithubIssueCommentResponse response, User author, Issue issue) {
    return IssueCommentCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .author(author)
        .issue(issue)
        .body(response.body())
        .build();
  }

  public ReviewCommand toReviewCommand(GithubReviewResponse response, User author, Pull pull) {
    return ReviewCommand.builder()
        .id(response.id())
        .submittedAt(response.submittedAt())
        .body(response.body())
        .author(author)
        .pull(pull)
        .build();
  }

  public PullCommentCommand toPullCommentCommand(
      GithubPullCommentResponse response, User author, Review review) {
    return PullCommentCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .author(author)
        .body(response.body())
        .review(review)
        .diffHunk(response.diffHunk())
        .path(response.path())
        .build();
  }
}
