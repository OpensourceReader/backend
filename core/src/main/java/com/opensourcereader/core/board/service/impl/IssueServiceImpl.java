package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.exception.IssueNotFoundException;
import com.opensourcereader.core.board.repository.IssueRepository;
import com.opensourcereader.core.board.service.IssueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;

  @Override
  @Transactional
  public Issue create(BoardBaseCommand command) {
    // TODO 비활성화된 것 제외하고 가져와야 함
    Issue issue =
        issueRepository
            .findById(command.getId())
            .orElseGet(
                () -> {
                  Issue entity = Issue.from(command);
                  return issueRepository.save(entity);
                });
    // TODO 만약 updatedAt가 command의 updatedAt랑 다르면 update 작업을 진행하는 로직 만들어야 함
    if (issue.getUpdatedAt() != command.getUpdatedAt()) {
      log.info("업데이트 로직 필요");
    }
    return issue;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existedByTagId(Long repositoryId, Long tagId) {
    return issueRepository.existsByRepositoryIdAndTagId(repositoryId, tagId);
  }

  @Override
  @Transactional(readOnly = true)
  public Issue findByTagId(Long repositoryId, Long tagId) {
    return issueRepository
        .findByRepositoryIdAndTagId(repositoryId, tagId)
        .orElseThrow(IssueNotFoundException::new);
  }

  // 복합 인덱스 검색
  @Override
  @Transactional(readOnly = true)
  public List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return issueRepository.findAllByRepositoryIdAndIsOpened(repositoryId, isOpened);
  }

  @Override
  @Transactional
  public void deleteSoftById(Long id) {
    Issue issue = issueRepository.findById(id).orElseThrow(IssueNotFoundException::new);
    issue.updateDisabled(true);
    issueRepository.save(issue);
  }
}
