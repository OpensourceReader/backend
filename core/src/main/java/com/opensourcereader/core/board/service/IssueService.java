package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.board.entity.Issue;

public interface IssueService {

  Issue create(BoardBaseCommand command);

  boolean existedByTagId(Long repositoryId, Long tagId);

  Issue findByTagId(Long repositoryId, Long tagId);

  List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isOpened);

  void deleteSoftById(Long id);
}
