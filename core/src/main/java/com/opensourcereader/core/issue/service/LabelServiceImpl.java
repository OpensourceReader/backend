package com.opensourcereader.core.issue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.issue.entity.Label;
import com.opensourcereader.core.issue.repository.LabelRepository;

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
}
