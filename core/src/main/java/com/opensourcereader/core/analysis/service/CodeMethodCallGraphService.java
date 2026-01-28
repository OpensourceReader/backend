package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;

public interface CodeMethodCallGraphService {

  void createMethodCallGraph(Long repoId, List<TypeStructureMeta> typeStructureMetas);

  DeclaredMethod getCodeMethodById(Long codeMethodId);
}
