package com.opensourcereader.core.analysis.dto;

import java.util.Arrays;
import java.util.List;

import aj.org.objectweb.asm.Type;

public record MethodDescriptor(String methodReturnType, List<String> argumentTypes) {

  public static MethodDescriptor from(String methodDescriptor) {
    return new MethodDescriptor(
        getReturnType(methodDescriptor), getArgumentTypes(methodDescriptor));
  }

  private static String getReturnType(String descriptor) {
    return Type.getReturnType(descriptor).getClassName();
  }

  private static List<String> getArgumentTypes(String descriptor) {
    return Arrays.stream(Type.getArgumentTypes(descriptor)).map(Type::getClassName).toList();
  }
}
