package com.opensourcereader.core.analysis.dto;

import java.util.List;

public record TypeStructureMeta(TypeInfo typeInfo, List<MethodStructure> methods) {

  public static List<TypeStructureMeta> from(List<TypeStructure> typeStructures) {
    return typeStructures.stream()
        .map(withSources -> new TypeStructureMeta(withSources.typeInfo(), withSources.methods()))
        .toList();
  }
}
