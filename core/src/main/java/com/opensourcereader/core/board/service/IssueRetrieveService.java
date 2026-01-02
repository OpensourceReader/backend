package com.opensourcereader.core.board.service;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.repository.PullRepository;
import java.util.List;

import java.util.Queue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.exception.BoardNotFoundException;
import com.opensourcereader.core.board.repository.IssueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueRetrieveService {

  private final IssueRepository issueRepository;
  private final PullRepository pullRepository;

  @Transactional(readOnly = true)
  public Queue<Integer> findPullByRepository(OpenSourceRepo repo) {
    return pullRepository.findAllByRepository(repo);
  }

  @Transactional(readOnly = true)
  public List<Issue> findIssuesByRepositoryId(Long repositoryId, Boolean isOpened) {
    return issueRepository.findAllByRepositoryIdAndIsOpenedOrderByCreatedAtDesc(
        repositoryId, isOpened);
  }

  @Transactional(readOnly = true)
  public Issue findIssueOrPullByTagId(Long repositoryId, Integer tagId) {
    Issue entity =
        issueRepository
            .findByRepositoryIdAndTagId(repositoryId, tagId)
            .orElseThrow(BoardNotFoundException::new);
    if (entity instanceof Pull pull) {
      return pull;
    } else {
      return entity;
    }
  }

  @Transactional(readOnly = true)
  public <T extends Issue> T findIssueOrPullByTagId(
      Long repositoryId, Integer tagId, Class<T> type) {
    Issue entity =
        issueRepository
            .findByRepositoryIdAndTagId(repositoryId, tagId)
            .orElseThrow(BoardNotFoundException::new);
    if (type.isInstance(entity)) {
      return type.cast(entity);
    } else {
      throw new BoardNotFoundException();
    }
  }

  @Transactional(readOnly = true)
  public Issue findIssueOrPullByTagId(String owner, String repoName, Integer tagId) {
    Issue entity =
        issueRepository
            .findByRepositoryOwnerLoginNameAndRepositoryTitleAndTagId(owner, repoName, tagId)
            .orElseThrow(BoardNotFoundException::new);
    if (entity instanceof Pull pull) {
      return pull;
    } else {
      return entity;
    }
  }

  @Transactional(readOnly = true)
  public List<Issue> findIssueOrPullWithComments(String owner, String repoName) {
    return issueRepository.findAllIssuesByRepo(owner, repoName, 1);
  }
}
