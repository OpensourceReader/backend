package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoService {

  OpenSourceRepo createRepo(
      String cloneUri,
      List<OpenSourceFileInfo> sourFileInfos,
      List<ClassStructure> classStructures);

  OpenSourceRepo getRepoById(Long repositoryId);

  void deleteRepoById(Long repositoryId);
}
