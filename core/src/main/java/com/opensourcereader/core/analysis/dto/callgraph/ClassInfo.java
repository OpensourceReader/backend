package com.opensourcereader.core.analysis.dto.callgraph;

import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import java.util.Arrays;
import java.util.List;

public record ClassInfo(
    int version,
    TypeKind typeKind,
    String className,
    String signature,
    String superName,
    List<String> interfaceNames) {

  public static ClassInfo of(
      int version,
      int access,
      String className,
      String signature,
      String superName,
      String[] interfaces) {
    return new ClassInfo(
        version, TypeKind.from(access), className, signature, superName, getInterfaces(interfaces));
  }

  private static List<String> getInterfaces(String[] interfaces) {
    if (interfaces == null) {
      return List.of();
    }
    return Arrays.asList(interfaces);
  }
}
