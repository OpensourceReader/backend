package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.domain.entity.Type;

public interface TypeHierarchyService {

  List<Type> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas);
}
