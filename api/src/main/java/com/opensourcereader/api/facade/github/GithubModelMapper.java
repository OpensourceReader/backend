package com.opensourcereader.api.facade.github;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.user.entity.User;

@Component
public class GithubModelMapper {
  public BoardBaseCommand toIssueCommand(
      GithubIssueResponse response, User author, OpenSourceRepo repo, Boolean isOpened) {
    return BoardBaseCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .tagId(response.tagId())
        .author(author)
        .repo(repo)
        .title(response.title())
        .body(response.body())
        .isOpened(isOpened)
        .commentCount(response.commentCount())
        .build();
  }

  public PullCommand toPullCommand(
      GithubIssueResponse response, User author, OpenSourceRepo repo, Boolean isOpened) {
    // TODO 리뷰수 카운팅해야함
    return PullCommand.builder()
        .id(response.id())
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .tagId(response.tagId())
        .author(author)
        .repo(repo)
        .title(response.title())
        .body(response.body())
        .isOpened(isOpened)
        .commentCount(response.commentCount())
        .build();
  }
}
