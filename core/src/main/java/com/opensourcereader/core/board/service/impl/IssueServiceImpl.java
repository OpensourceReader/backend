package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.exception.IssueNotFoundException;
import com.opensourcereader.core.board.repository.IssueRepository;
import com.opensourcereader.core.board.service.IssueService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;

  @Override
  public boolean existedByTagId(Long repositoryId, Long tagId) {
    return issueRepository.existsByRepositoryIdAndTagId(repositoryId, tagId);
  }

  @Override
  public Issue findByTagId(Long repositoryId, Long tagId) {
    return issueRepository
        .findByRepositoryIdAndTagId(repositoryId, tagId)
        .orElseThrow(IssueNotFoundException::new);
  }

  // 복합 인덱스 검색
  @Override
  public List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return issueRepository.findAllByRepositoryIdAndIsOpened(repositoryId, isOpened);
  }
}
