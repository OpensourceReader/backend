package com.opensourcereader.core.analysis.dto.callgraph;

public record MethodCallEdge(
    MethodInfo caller, MethodInfo callee, int operationCode, boolean isCalleeMethodInterface) {

  public static MethodCallEdge of(
      String callerClassName,
      String callerMethodName,
      String callerDescriptor,
      String calleeClassName,
      String calleeMethodName,
      String calleeDescription,
      int operationCode,
      boolean isCalleeMethodInterface) {
    return new MethodCallEdge(
        MethodInfo.of(callerClassName, callerMethodName, callerDescriptor),
        MethodInfo.of(calleeClassName, calleeMethodName, calleeDescription),
        operationCode,
        isCalleeMethodInterface);
  }
}
