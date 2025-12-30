package com.opensourcereader.core.board.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
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
import org.yaml.snakeyaml.util.Tuple;

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

  @Transactional
  public void syncIssues(Queue<BoardBaseCommand> commands) {
    List<BoardBaseCommand> saveCommands = new ArrayList<>();
    List<PairIssue> updateCommands = new ArrayList<>();

    while (!commands.isEmpty()) {
      BoardBaseCommand command = commands.poll();
      try {
        Issue existingIssue =
            issueRepository
                .findByRepositoryIdAndTagId(command.getRepo().getId(), command.getTagId())
                .orElse(null);
        if (existingIssue == null) {
          saveCommands.add(command);
        } else {
          updateCommands.add(new PairIssue(existingIssue, command));
        }
      } catch (Exception e) {
        commands.add(command);
      }
    }

    saveNesIssues(saveCommands);
    updateIssues(updateCommands);
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

  private void saveNesIssues(List<BoardBaseCommand> commands) {
    List<Issue> entityList = new ArrayList<>();
    for (BoardBaseCommand command : commands) {
      Issue entity;
      if (command instanceof PullCommand pullCommand) {
        entity = Pull.from(pullCommand);
      } else {
        entity = Issue.from(command);
      }
      entityList.add(entity);
    }
    issueRepository.saveAll(entityList);
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

  private void updateIssues(List<PairIssue> updatePair) {
    for (PairIssue pair : updatePair) {
      updateIssue(pair.issue(), pair.command());
    }
  }
}

record PairIssue(Issue issue, BoardBaseCommand command) {
}