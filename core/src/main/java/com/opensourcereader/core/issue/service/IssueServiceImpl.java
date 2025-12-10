package com.opensourcereader.core.issue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.issue.entity.Issue;
import com.opensourcereader.core.issue.exception.issue.IssueNotFoundException;
import com.opensourcereader.core.issue.repository.IssueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;

  @Override
  public Issue findByTagId(Long repositoryId, Long tagId) {
    return issueRepository
        .findByRepositoryIdAndTagId(repositoryId, tagId)
        .orElseThrow(IssueNotFoundException::new);
  }

  // 복합 인덱스 검색
  @Override
  public List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return issueRepository.findAllByRepositoryIdAndIsClosed(repositoryId, isOpened);
  }
}
