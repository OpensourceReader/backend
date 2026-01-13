package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;

public interface CodeMethodCallGraphService {

  void createMethodCallGraph(Long repoId, List<ClassStructure> classStructures);

  DeclaredMethod getCodeMethodById(Long codeMethodId);
}
