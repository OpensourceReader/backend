package com.opensourcereader.core.issue.service;

import java.util.List;

import com.opensourcereader.core.issue.entity.Issue;

public interface IssueService {

  Issue findByTagId(Long repositoryId, Long tagId);

  List<Issue> findAllByRepositoryId(Long repositoryId, Boolean isClosed);
}
