package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.dto.PullCommand;
import com.opensourcereader.core.board.entity.Pull;

public interface PullService {

  Pull create(PullCommand command);

  boolean existedByTagId(Long repositoryId, Long tagId);

  Pull findByTagId(Long repositoryId, Long tagId);

  List<Pull> findAllByRepositoryId(Long repositoryId, Boolean isOpened);

  void deleteSoftById(Long id);
}
