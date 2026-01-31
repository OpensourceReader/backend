package com.opensourcereader.core.analysis.infra.dto;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;

public record ByteCodeDeclaredMethodInfo(
    String className,
    String methodName,
    EnumSet<MethodModifier> methodModifiers,
    MethodDescriptor methodDescriptor,
    String genericSignature,
    List<String> exceptions) {

  public static ByteCodeDeclaredMethodInfo of(
      String className,
      int classAccess,
      int methodAccess,
      String methodName,
      String descriptor,
      String genericSignature,
      String[] exceptions) {
    return new ByteCodeDeclaredMethodInfo(
        className,
        methodName,
        MethodModifier.from(classAccess, methodAccess),
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
