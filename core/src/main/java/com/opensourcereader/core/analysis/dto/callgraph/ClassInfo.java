package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.Arrays;
import java.util.List;

public record ClassInfo(
    int version,
    int access,
    String className,
    String signature,
    String superName,
    List<String> interfacePaths) {

  public static ClassInfo of(
      int version,
      int access,
      String className,
      String signature,
      String superName,
      String[] interfaces) {
    return new ClassInfo(
        version, access, className, signature, superName, getInterfaces(interfaces));
  }

  private static List<String> getInterfaces(String[] interfaces) {
    if (interfaces == null) {
      return List.of();
    }
    return Arrays.asList(interfaces);
  }
}
