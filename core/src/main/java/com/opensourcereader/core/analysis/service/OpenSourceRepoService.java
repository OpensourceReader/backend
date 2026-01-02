package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.user.entity.User;

public interface OpenSourceRepoService {

  OpenSourceRepo getOrCreateRepoInDB(User owner, String title);

  OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference);

  OpenSourceRepo getRepoById(Long repositoryId);

  OpenSourceRepo getRepoByOwnerNameAndTitle(String ownerName, String title);

  void deleteRepoById(Long repositoryId);
}
