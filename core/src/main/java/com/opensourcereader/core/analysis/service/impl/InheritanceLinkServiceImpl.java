package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.service.InheritanceLinkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InheritanceLinkServiceImpl implements InheritanceLinkService {

  @Override
  public void resolve(List<Type> types, List<TypeStructure> typeStructures) {
    if (types == null || types.isEmpty() || typeStructures == null || typeStructures.isEmpty()) {
      return;
    }
    Map<String, Type> typeMap =
        types.stream().collect(Collectors.toMap(Type::getTypeInternalName, type -> type));
    for (TypeInfo typeInfo : extractTypeInfos(typeStructures)) {
      Type type = typeMap.get(typeInfo.typeInternalName());
      type.updateRelations(typeMap.get(typeInfo.superName()), getInterfaceTypes(typeInfo, typeMap));
    }
  }

  private List<Type> getInterfaceTypes(TypeInfo typeInfo, Map<String, Type> typeMap) {
    return typeInfo.interfaceNames().stream().map(typeMap::get).filter(Objects::nonNull).toList();
  }

  private List<TypeInfo> extractTypeInfos(List<TypeStructure> typeStructures) {
    return typeStructures.stream().map(TypeStructure::typeInfo).filter(Objects::nonNull).toList();
  }
}
