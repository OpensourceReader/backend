package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference);

  OpenSourceRepo getRepoById(Long repositoryId);

  void deleteRepoById(Long repositoryId);
}
