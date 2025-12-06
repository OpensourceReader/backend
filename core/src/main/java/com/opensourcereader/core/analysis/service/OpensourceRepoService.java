package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;

public interface OpensourceRepoService {

  OpensourceRepo create(GitTree gitTree);

  OpensourceRepo get(Long repositoryId);

  void delete(Long repositoryId);

}
