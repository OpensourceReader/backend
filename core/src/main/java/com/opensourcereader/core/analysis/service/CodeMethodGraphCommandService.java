package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;

public interface CodeMethodGraphCommandService {

  List<CodeMethod> createMethodCallGraph(Long repoId, List<ClassStructure> classStructures);
}
