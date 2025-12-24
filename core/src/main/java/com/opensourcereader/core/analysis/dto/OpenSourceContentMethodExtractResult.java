package com.opensourcereader.core.analysis.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.opensourcereader.core.analysis.entity.codedetail.MethodModifier;

public record OpenSourceContentMethodExtractResult(
    String methodName,
    MethodModifier modifier,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {

  public static OpenSourceContentMethodExtractResult of(MethodDeclaration methodDeclaration) {
    return new OpenSourceContentMethodExtractResult(
        methodDeclaration.getNameAsString(),
        MethodModifier.from(methodDeclaration),
        getParameterTypes(methodDeclaration),
        getStartLine(methodDeclaration),
        getEndLine(methodDeclaration));
  }

  private static List<String> getParameterTypes(MethodDeclaration methodDeclaration) {
    List<String> paramTypes = new ArrayList<>();
    for (Parameter methodParameter : methodDeclaration.getParameters()) {
      paramTypes.add(methodParameter.getType().toString());
    }
    return paramTypes;
  }

  private static Integer getStartLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().begin.line;
  }

  private static Integer getEndLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().end.line;
  }
}
