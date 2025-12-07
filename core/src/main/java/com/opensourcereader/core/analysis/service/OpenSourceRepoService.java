package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo create(GitTree gitTree);

  OpenSourceRepo get(Long repositoryId);

  void delete(Long repositoryId);

}
