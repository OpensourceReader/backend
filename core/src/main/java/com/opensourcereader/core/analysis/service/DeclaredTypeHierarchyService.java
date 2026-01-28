package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;

public interface DeclaredTypeHierarchyService {

  List<DeclaredType> resolveTypeHierarchy(List<TypeStructureMeta> typeStructureMetas);
}
