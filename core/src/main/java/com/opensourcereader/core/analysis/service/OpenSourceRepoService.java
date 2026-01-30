package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(String cloneUri, List<TypeStructure> typeStructures);

  OpenSourceRepo getRepoById(Long repositoryId);

  void deleteRepoById(Long repositoryId);
}
