package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.entity.OpensourceRepo;

public interface OpensourceRepoService {

  OpensourceRepo create(String treeApiUrl);

  OpensourceRepo get(Long repositoryId);

  void delete(Long repositoryId);

}
