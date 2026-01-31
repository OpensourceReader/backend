package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.Type;

public interface TypeHierarchyService {

  List<Type> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas);
}
