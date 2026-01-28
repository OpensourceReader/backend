package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.service.DeclaredTypeHierarchyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeclaredTypeHierarchyServiceImpl implements DeclaredTypeHierarchyService {

  private final DeclaredTypeRepository declaredTypeRepository;

  public List<DeclaredType> resolveTypeHierarchy(List<TypeStructureMeta> typeStructureMetas) {
    if (typeStructureMetas == null || typeStructureMetas.isEmpty()) {
      return List.of();
    }

    List<DeclaredType> declaredTypes = new ArrayList<>();
    for (TypeInfo typeInfo : getClassInfos(typeStructureMetas)) {
      DeclaredType declaredType =
          declaredTypeRepository
              .findByTypeInternalName(typeInfo.internalName())
              .orElseThrow(IllegalArgumentException::new);
      DeclaredType superType = findTypeOrExternal(typeInfo.superName());
      List<DeclaredTypeImplementEdge> implementEdges = getImplementEdges(typeInfo, declaredType);

      declaredType.update(superType, implementEdges);
      declaredTypes.add(declaredType);
    }

    return declaredTypeRepository.saveAll(declaredTypes);
  }

  private List<DeclaredTypeImplementEdge> getImplementEdges(
      TypeInfo typeInfo, DeclaredType declaredType) {
    return typeInfo.interfaceNames().stream()
        .map(this::findTypeOrExternal)
        .map(interfaceType -> DeclaredTypeImplementEdge.of(declaredType, interfaceType))
        .toList();
  }

  private DeclaredType findTypeOrExternal(String superName) {
    if (superName == null) {
      return null;
    }
    return declaredTypeRepository
        .findByTypeInternalName(superName)
        .orElse(DeclaredType.external(superName));
  }

  private List<TypeInfo> getClassInfos(List<TypeStructureMeta> typeStructureMetas) {
    return typeStructureMetas.stream().map(TypeStructureMeta::typeInfo).toList();
  }
}
