package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Label;
import com.opensourcereader.core.board.repository.LabelRepository;
import com.opensourcereader.core.board.service.LabelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

  private final LabelRepository labelRepository;

  // 인덱스 검색
  @Override
  public List<Label> findAllByRepositoryId(Long repositoryId) {
    return labelRepository.findAllByRepositoryId(repositoryId);
  }

  @Override
  public List<Label> findAllByIssue(Issue issue) {
    return labelRepository.findAllByIssue(issue);
  }
}
