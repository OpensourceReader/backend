package com.opensourcereader.api.facade.github;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import org.springframework.stereotype.Component;

import com.opensourcereader.api.client.response.GithubIssueResponse;
import com.opensourcereader.api.client.response.GithubPullResponse;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.user.entity.User;

@Component
public class GithubModelMapper {
  public BoardBaseCommand toCommand(GithubIssueResponse response, User author, OpenSourceRepo repo, Boolean isOpened) {
    BoardBaseCommand command = new BoardBaseCommand();
    command.setId(response.id());
    command.setCreatedAt(response.createdAt());
    command.setUpdatedAt(response.updatedAt());
    command.setTagId(response.tagId());
    command.setAuthor(author);
    command.setRepo(repo);
    command.setTitle(response.title());
    command.setBody(response.body());
    command.setIsOpened(isOpened);
    command.setCommentCount(response.commentCount());
    return command;
  }

  public PullCommand toCommand(GithubPullResponse response, User author, OpenSourceRepo repo, Boolean isOpened) {
    PullCommand command = new PullCommand();
    command.setId(response.id());
    command.setCreatedAt(response.createdAt());
    command.setUpdatedAt(response.updatedAt());
    command.setTagId(response.tagId());
    command.setAuthor(author);
    command.setRepo(repo);
    command.setTitle(response.title());
    command.setBody(response.body());
    command.setIsOpened(isOpened);
    command.setCommentCount(response.commentCount());

    // TODO 리뷰수 카운팅해야함
    return command;
  }

}
