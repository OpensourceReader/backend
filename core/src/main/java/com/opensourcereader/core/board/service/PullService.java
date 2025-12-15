package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.Pull;

public interface PullService {

  Pull findByTagId(Long repositoryId, Long tagId);

  List<Pull> findAllByRepositoryId(Long repositoryId, Boolean isOpened);
}
