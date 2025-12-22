package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.exception.IssueNotFoundException;
import com.opensourcereader.core.board.repository.IssueRepository;
import com.opensourcereader.core.board.service.IssueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;

  @Override
  @Transactional
  public Issue upsert(BoardBaseCommand command) {
    Issue entity = Issue.from(command);
    issueRepository.upsert(entity);
    return entity;
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
