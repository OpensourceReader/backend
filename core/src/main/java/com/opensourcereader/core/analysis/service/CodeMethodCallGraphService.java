package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;
import com.opensourcereader.core.analysis.entity.methodcall.CodeMethodCallEdge;

public interface CodeMethodCallGraphService {

  List<CodeMethodCallEdge> createMethodCallGraph(Long repoId, List<ClassStructure> classStructures);

  CodeMethod getCodeMethodById(Long codeMethodId);
}
