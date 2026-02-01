package com.opensourcereader.core.analysis.dto;

import java.util.Arrays;
import java.util.List;

import com.opensourcereader.core.analysis.entity.type.TypeKind;

public record TypeInfo(
    int version,
    TypeKind typeKind,
    String typeInternalName,
    String signature,
    String superName,
    List<String> interfaceNames) {

  public static TypeInfo of(
      int version,
      int access,
      String className,
      String signature,
      String superName,
      String[] interfaces) {
    return new TypeInfo(
        version, TypeKind.from(access), className, signature, superName, getInterfaces(interfaces));
  }

  private static List<String> getInterfaces(String[] interfaces) {
    if (interfaces == null) {
      return List.of();
    }
    return Arrays.asList(interfaces);
  }
}
