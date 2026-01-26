package com.opensourcereader.api.dto;

import java.util.List;

import com.opensourcereader.core.analysis.entity.method.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;

public record CodeMethodSummary(Long id, String classInternalName, String methodName) {

  public static List<CodeMethodSummary> ingoing(List<CodeMethodCallEdge> methodCalls) {
    return methodCalls.stream()
        .map(CodeMethodCallEdge::getCaller)
        .map(CodeMethodSummary::from)
        .toList();
  }

  public static List<CodeMethodSummary> outgoing(List<CodeMethodCallEdge> methodCalls) {
    return methodCalls.stream()
        .map(CodeMethodCallEdge::getCallee)
        .map(CodeMethodSummary::from)
        .toList();
  }

  public static CodeMethodSummary from(DeclaredMethod declaredMethod) {
    return new CodeMethodSummary(
        declaredMethod.getId(),
        declaredMethod.getTypeInternalName(),
        declaredMethod.getMethodName());
  }
}
