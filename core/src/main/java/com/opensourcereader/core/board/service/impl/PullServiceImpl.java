package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.exception.PullNotFoundException;
import com.opensourcereader.core.board.repository.PullRepository;
import com.opensourcereader.core.board.service.PullService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PullServiceImpl implements PullService {

  private final PullRepository pullRepository;

  @Override
  public Pull findByTagId(Long repositoryId, Long tagId) {
    return pullRepository
        .findByRepositoryIdAndTagId(repositoryId, tagId)
        .orElseThrow(PullNotFoundException::new);
  }

  @Override
  public List<Pull> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return pullRepository.findAllByRepositoryIdAndIsOpened(repositoryId, isOpened);
  }
}
