package com.opensourcereader.core.board.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.repository.IssueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueSyncService {

  private final IssueRepository issueRepository;

  @Transactional
  public void syncIssue(BoardBaseCommand command) {
    Issue existingIssue =
        issueRepository
            .findByRepositoryIdAndTagId(command.getRepo().getId(), command.getTagId())
            .orElse(null);

    if (existingIssue == null) {
      Issue entity;
      if (command instanceof PullCommand pullCommand) {
        entity = Pull.from(pullCommand);
      } else {
        entity = Issue.from(command);
      }
      issueRepository.save(entity);
    } else {
      existingIssue.updateBody(command.getBody());
      existingIssue.updateStatus(command.getIsOpened());
      existingIssue.updateCommentCount(command.getCommentCount());
    }
  }
}
