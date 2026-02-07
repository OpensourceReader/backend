package com.opensourcereader.api.dto;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.MethodCallEdge;

public record CodeMethodSummary(Long id, String classInternalName, String methodName) {

  public static List<CodeMethodSummary> ingoing(List<MethodCallEdge> methodCalls) {
    return methodCalls.stream()
        .map(MethodCallEdge::getCaller)
        .map(CodeMethodSummary::from)
        .toList();
  }

  public static List<CodeMethodSummary> outgoing(List<MethodCallEdge> methodCalls) {
    return methodCalls.stream()
        .map(MethodCallEdge::getCallee)
        .map(CodeMethodSummary::from)
        .toList();
  }

  public static CodeMethodSummary from(Method method) {
    return new CodeMethodSummary(
        method.getId(), method.getType().getTypeInternalName(), method.getMethodName());
  }
}
