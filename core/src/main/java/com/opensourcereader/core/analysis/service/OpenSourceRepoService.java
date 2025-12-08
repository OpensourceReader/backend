package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(GitTree gitTree);

  OpenSourceRepo getRepoById(Long repositoryId);

  void deleteRepoById(Long repositoryId);

}
