package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.Issue;

public interface IssueService {

  Issue findByTagId(Long repositoryId, Long tagId);

  List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isOpened);
}
