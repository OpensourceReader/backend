package com.opensourcereader.core.analysis.dto.callgraph.method;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.entity.codedetail.AccessModifier;
import com.opensourcereader.core.analysis.entity.codedetail.NonAccessModifier;

public record DeclaredMethodInfo(
    AccessModifier accessModifier,
    EnumSet<NonAccessModifier> nonAccessModifiers,
    String className,
    String methodName,
    MethodDescriptor methodDescriptor,
    List<String> exceptions) {

  public static DeclaredMethodInfo of(
      int access, String className, String methodName, String descriptor, String[] exceptions) {
    return new DeclaredMethodInfo(
        AccessModifier.from(access),
        NonAccessModifier.from(access),
        className,
        methodName,
        MethodDescriptor.from(descriptor),
        Arrays.stream(exceptions).toList());
  }
}
