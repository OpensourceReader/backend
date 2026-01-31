package com.opensourcereader.core.analysis.dto;

import java.util.List;

public record TypeStructureMeta(TypeInfo typeInfo, List<MethodStructure> methods) {

  public static List<TypeStructureMeta> from(List<TypeStructure> typeStructures) {
    return typeStructures.stream()
        .map(withSources -> new TypeStructureMeta(withSources.typeInfo(), withSources.methods()))
        .toList();
  }

  public record MethodCallInfo(
      int operationCode,
      String typeInternalName,
      String methodName,
      MethodDescriptor descriptor,
      boolean isInterface) {

    public static MethodCallInfo of(
        int opcode,
        String calleeOwnerClassName,
        String calleeMethodName,
        String calleeDescriptor,
        boolean isInterface) {
      return new MethodCallInfo(
          opcode,
          calleeOwnerClassName,
          calleeMethodName,
          MethodDescriptor.from(calleeDescriptor),
          isInterface);
    }
  }
}
