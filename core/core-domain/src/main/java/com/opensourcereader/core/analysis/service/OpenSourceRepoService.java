package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.dto.TypeStructure;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(String cloneUri, List<TypeStructure> typeStructures);

  OpenSourceRepo getRepoById(Long repositoryId);

  OpenSourceRepo getRepoByOwnerNameAndRepoName(String ownerName, String title);

  void deleteRepoById(Long repositoryId);
}
