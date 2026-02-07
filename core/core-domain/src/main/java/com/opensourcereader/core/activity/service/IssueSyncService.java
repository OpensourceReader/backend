package com.opensourcereader.core.activity.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.activity.dto.BoardBaseCommand;
import com.opensourcereader.core.activity.dto.PairIssue;
import com.opensourcereader.core.activity.dto.PullCommand;
import com.opensourcereader.core.activity.entity.Issue;
import com.opensourcereader.core.activity.entity.Pull;
import com.opensourcereader.core.activity.exception.IssueNotFoundException;
import com.opensourcereader.core.activity.repository.IssueRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueSyncService {

  // 최대 재시도 횟수
  private static final int MAX_RETRY_COUNT = 3;
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
  public boolean syncIssues(Queue<BoardBaseCommand> commands) {
    List<BoardBaseCommand> saveCommands = new ArrayList<>();
    List<PairIssue> updateCommands = new ArrayList<>();

    Map<Integer, Integer> retryCounts = new HashMap<>();

    while (!commands.isEmpty()) {
      BoardBaseCommand command = commands.poll();

      if (command == null || command.getTagId() == null) continue;

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
        Integer tagId = command.getTagId();
        int currentRetry = retryCounts.getOrDefault(tagId, 0);

        if (currentRetry < MAX_RETRY_COUNT) {
          retryCounts.put(tagId, currentRetry + 1);
          commands.add(command);
          log.warn(
              "Retrying issue sync for tagId: {} (Attempt {}/{}) - Error: {}",
              tagId,
              currentRetry + 1,
              MAX_RETRY_COUNT,
              e.getMessage());
        } else {
          // 최대 횟수 초과 시 포기 (DLQ 전송 혹은 에러 로그 기록)
          log.error(
              "Failed to sync issue after {} retries for tagId: {}. Command discarded.",
              MAX_RETRY_COUNT,
              tagId,
              e);
        }
      }
    }

    saveNesIssues(saveCommands);
    updateIssues(updateCommands);

    return true;
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
