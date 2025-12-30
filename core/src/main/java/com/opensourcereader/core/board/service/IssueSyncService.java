package com.opensourcereader.core.board.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.exception.IssueNotFoundException;
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
      try {
        saveNewIssue(command);
      } catch (DataIntegrityViolationException e) {
        retryUpdate(command);
      }
    } else {
      updateIssue(existingIssue, command);
    }
  }

  private void saveNewIssue(BoardBaseCommand command) {
    Issue entity;
    if (command instanceof PullCommand pullCommand) {
      entity = Pull.from(pullCommand);
    } else {
      entity = Issue.from(command);
    }
    issueRepository.save(entity);
  }

  private void retryUpdate(BoardBaseCommand command) {
    Issue existingIssue =
        issueRepository
            .findByRepositoryIdAndTagId(command.getRepo().getId(), command.getTagId())
            .orElseThrow(IssueNotFoundException::new);
    updateIssue(existingIssue, command);
  }

  private void updateIssue(Issue issue, BoardBaseCommand command) {
    issue.updateBody(command.getBody());
    issue.updateStatus(command.getIsOpened());
    issue.updateCommentCount(command.getCommentCount());
    if (issue instanceof Pull pull && command instanceof PullCommand pullCommand) {
      pull.updateReviewCount(pullCommand.getReviewCount());
    }
  }
}
