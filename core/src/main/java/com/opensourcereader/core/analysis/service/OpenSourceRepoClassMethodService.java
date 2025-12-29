package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethod;

public interface OpenSourceRepoClassMethodService {

  List<CodeMethod> createMethodCallGraph(List<ClassStructure> classStructures);
}
