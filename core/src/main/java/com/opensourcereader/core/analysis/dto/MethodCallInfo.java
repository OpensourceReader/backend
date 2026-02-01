package com.opensourcereader.core.analysis.dto;

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