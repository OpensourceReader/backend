package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(Path savedLocalPath, String cloneUrl, String repoReference);

  OpenSourceRepo getRepoById(Long repositoryId);

  void deleteRepoById(Long repositoryId);
}
