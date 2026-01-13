package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.service.DeclaredTypeHierarchyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeclaredTypeHierarchyServiceImpl implements DeclaredTypeHierarchyService {

  private final DeclaredTypeRepository declaredTypeRepository;

  public List<DeclaredType> resolveTypeHierarchy(List<ClassStructure> classStructures) {
    if (classStructures == null || classStructures.isEmpty()) {
      return List.of();
    }

    List<DeclaredType> declaredTypes = new ArrayList<>();
    for (ClassInfo classInfo : getClassInfos(classStructures)) {
      DeclaredType declaredType =
          declaredTypeRepository
              .findByTypeInternalName(classInfo.className())
              .orElseThrow(IllegalArgumentException::new);
      DeclaredType superType = findTypeOrExternal(classInfo.superName());
      List<DeclaredTypeImplementEdge> implementEdges = getImplementEdges(classInfo, declaredType);

      declaredType.update(superType, implementEdges);
      declaredTypes.add(declaredType);
    }

    return declaredTypeRepository.saveAll(declaredTypes);
  }

  private List<DeclaredTypeImplementEdge> getImplementEdges(
      ClassInfo classInfo, DeclaredType declaredType) {
    return classInfo.interfaceNames().stream()
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

  private List<ClassInfo> getClassInfos(List<ClassStructure> classStructures) {
    return classStructures.stream().map(ClassStructure::classInfo).toList();
  }
}
