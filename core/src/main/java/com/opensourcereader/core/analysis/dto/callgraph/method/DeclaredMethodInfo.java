package com.opensourcereader.core.analysis.dto.callgraph.method;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.entity.method.MethodModifier;

public record DeclaredMethodInfo(
    String className,
    EnumSet<MethodModifier> methodModifiers,
    String methodName,
    MethodDescriptor methodDescriptor,
    String genericSignature,
    List<String> exceptions) {

  public static DeclaredMethodInfo of(
      String className,
      int classAccess,
      int methodAccess,
      String methodName,
      String descriptor,
      String genericSignature,
      String[] exceptions) {
    return new DeclaredMethodInfo(
        className,
        MethodModifier.from(classAccess, methodAccess),
        methodName,
        MethodDescriptor.from(descriptor),
        genericSignature,
        getExceptions(exceptions));
  }

  private static List<String> getExceptions(String[] exceptions) {
    if (exceptions == null) {
      return List.of();
    }
    return Arrays.asList(exceptions);
  }
}
