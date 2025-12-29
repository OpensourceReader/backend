package com.opensourcereader.core.analysis.dto.callgraph.method;

public record MethodCallInfo(
    int operationCode,
    String className,
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
