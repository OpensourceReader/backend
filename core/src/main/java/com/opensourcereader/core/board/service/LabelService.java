package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Label;

public interface LabelService {

  List<Label> findAllByRepositoryId(Long repositoryId);

  List<Label> findAllByIssue(Issue issue);
}
