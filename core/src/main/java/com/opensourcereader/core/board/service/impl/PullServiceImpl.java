package com.opensourcereader.core.board.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

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
  public Pull create(PullCommand command) {
    Pull pull =
        pullRepository
            .findById(command.getId())
            .orElseGet(
                () -> {
                  Pull entity = Pull.from(command);
                  return pullRepository.save(entity);
                });
    // TODO 만약 updatedAt가 command의 updatedAt랑 다르면 update 작업을 진행하는 로직 만들어야 함
    if (pull.getUpdatedAt() != command.getUpdatedAt()) {
      log.info("업데이트 로직 필요");
    }

    return pull;
  }

  @Override
  public boolean existedByTagId(Long repositoryId, Long tagId) {
    return pullRepository.existsByRepositoryIdAndTagId(repositoryId, tagId);
  }

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

  @Override
  public void deleteSoftById(Long id) {
    Pull pull = pullRepository.findById(id).orElseThrow(PullNotFoundException::new);
    pull.updateDisabled(true);
    pullRepository.save(pull);
  }
}
