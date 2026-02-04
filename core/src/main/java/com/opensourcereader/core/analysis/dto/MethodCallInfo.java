package com.opensourcereader.core.analysis.dto;

public record MethodCallInfo(
    int operationCode,
    String calleeTypeInternalName,
    String methodName,
    MethodDescriptor descriptor,
    boolean isInterface) {

  public static MethodCallInfo of(
      int opcode,
      String calleeTypeName,
      String calleeMethodName,
      String calleeDescriptor,
      boolean isInterface) {
    return new MethodCallInfo(
        opcode,
        calleeTypeName,
        calleeMethodName,
        MethodDescriptor.from(calleeDescriptor),
        isInterface);
  }
}
