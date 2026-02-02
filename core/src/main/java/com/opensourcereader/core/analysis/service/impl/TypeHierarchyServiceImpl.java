package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.repository.TypeRepository;
import com.opensourcereader.core.analysis.service.TypeHierarchyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeHierarchyServiceImpl implements TypeHierarchyService {

  private final TypeRepository typeRepository;

  public List<Type> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    if (repoId == null || typeStructureMetas == null || typeStructureMetas.isEmpty()) {
      return List.of();
    }
    List<Type> types = new ArrayList<>();
    for (TypeInfo typeInfo : getRepoInnerTypeInfos(typeStructureMetas)) {
      Type type =
          typeRepository
              .findByRepoAndTypeInternalName(repoId, typeInfo.typeInternalName())
              .orElseThrow(IllegalArgumentException::new);
      type.updateRelations(
          getUpperType(repoId, typeInfo.superName()), getInterfaceTypes(repoId, typeInfo));
      types.add(type);
    }

    return typeRepository.saveAll(types);
  }

  private List<Type> getInterfaceTypes(Long repoId, TypeInfo typeInfo) {
    return typeInfo.interfaceNames().stream()
        .map(interfaceName -> this.getUpperType(repoId, interfaceName))
        .toList();
  }

  // external에서 없으면 안들어갈 수도 있음
  private Type getUpperType(Long repoId, String typeName) {
    if (typeName == null) {
      return null;
    }
    return typeRepository
        .findByRepoAndTypeInternalName(repoId, typeName)
        .orElseThrow(IllegalArgumentException::new);
  }

  private List<TypeInfo> getRepoInnerTypeInfos(List<TypeStructureMeta> typeStructureMetas) {
    return typeStructureMetas.stream().map(TypeStructureMeta::typeInfo).toList();
  }
}
