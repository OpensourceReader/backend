package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;

public interface DeclaredTypeRelationService {

  List<DeclaredType> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas);
}
