package com.opensourcereader.core.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.repository.PullRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRetrieveService {

  private final PullRepository pullRepository;

  @Transactional(readOnly = true)
  public List<Pull> findAllByRepositoryId(Long repositoryId, Boolean isOpened) {
    return pullRepository.findAllByRepositoryIdAndIsOpened(repositoryId, isOpened);
  }
}
