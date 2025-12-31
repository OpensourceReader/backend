package com.opensourcereader.api.dto;

import java.util.List;

import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;

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

  public static CodeMethodSummary from(CodeMethod codeMethod) {
    return new CodeMethodSummary(
        codeMethod.getId(), codeMethod.getClassInternalName(), codeMethod.getMethodName());
  }
}
