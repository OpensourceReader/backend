package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;

public interface DeclaredTypeHierarchyService {

  List<DeclaredType> resolveTypeHierarchy(List<ClassStructure> classStructures);
}
