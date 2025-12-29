package com.opensourcereader.core.analysis.dto.callgraph.method;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.entity.codemethod.AccessModifier;
import com.opensourcereader.core.analysis.entity.codemethod.NonAccessModifier;

public record DeclaredMethodInfo(
    String className,
    AccessModifier accessModifier,
    EnumSet<NonAccessModifier> nonAccessModifiers,
    String methodName,
    MethodDescriptor methodDescriptor,
    String genericSignature,
    List<String> exceptions) {

  public static DeclaredMethodInfo of(
      String className,
      int access,
      String methodName,
      String descriptor,
      String genericSignature,
      String[] exceptions) {
    return new DeclaredMethodInfo(
        className,
        AccessModifier.from(access),
        NonAccessModifier.from(access),
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
