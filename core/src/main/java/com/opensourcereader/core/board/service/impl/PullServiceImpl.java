package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.exception.PullNotFoundException;
import com.opensourcereader.core.board.repository.PullRepository;
import com.opensourcereader.core.board.service.PullService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullServiceImpl implements PullService {

  private final PullRepository pullRepository;

  @Override
  @Transactional
  public Pull upsert(PullCommand command) {
    Pull entity = Pull.from(command);
    pullRepository.upsert(entity);
    return entity;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existedByTagId(Long repositoryId, Long tagId) {
    return pullRepository.existsByRepositoryIdAndTagId(repositoryId, tagId);
  }

  @Override
  @Transactional(readOnly = true)
  public Pull findByTagId(Long repositoryId, Long tagId) {
    return pullRepository
        .findByRepositoryIdAndTagId(repositoryId, tagId)
        .orElseThrow(PullNotFoundException::new);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pull> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return pullRepository.findAllByRepositoryIdAndIsOpened(repositoryId, isOpened);
  }

  @Override
  @Transactional
  public void deleteSoftById(Long id) {
    Pull pull = pullRepository.findById(id).orElseThrow(PullNotFoundException::new);
    pull.updateDisabled(true);
    pullRepository.save(pull);
  }
}
