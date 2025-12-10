package com.opensourcereader.core.issue.service;

import java.util.List;

import com.opensourcereader.core.issue.entity.Label;

public interface LabelService {

  List<Label> findAllByRepositoryId(Long repositoryId);
}
