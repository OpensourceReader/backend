package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.opensourcereader.core.analysis.dto.OpenSourceContentMethodExtractResult;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.entity.Extension;
import com.opensourcereader.core.analysis.entity.codedetail.MethodAccessModifier;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceMethodExtractor {

  private static final String VAR_ARGS_EXPRESSION = "...";

  private final JavaParser javaParser;

  public List<OpenSourceContentMethodExtractResult> extractCodeMethods(
      OpenSourceFileInfo openSourceFileInfo) {
    if (!Extension.isJavaFile(openSourceFileInfo.path())) {
      return List.of();
    }

    ParseResult<CompilationUnit> compilationUnit = javaParser.parse(openSourceFileInfo.rawText());
    if (!compilationUnit.isSuccessful() || compilationUnit.getResult().isEmpty()) {
      return List.of();
    }
    return compilationUnit.getResult().get().findAll(MethodDeclaration.class).stream()
        .map(
            methodDeclaration ->
                new OpenSourceContentMethodExtractResult(
                    methodDeclaration.getNameAsString(),
                    MethodAccessModifier.from(methodDeclaration),
                    getParameterTypes(methodDeclaration),
                    getStartLine(methodDeclaration),
                    getEndLine(methodDeclaration)))
        .toList();
  }

  private List<String> getParameterTypes(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getParameters().stream()
        .map(OpenSourceMethodExtractor::getParameterType)
        .toList();
  }

  private static String getParameterType(Parameter methodParameter) {
    Type type = methodParameter.getType();
    if (methodParameter.isVarArgs()) {
      return type + VAR_ARGS_EXPRESSION;
    }
    if (type.isClassOrInterfaceType()) {
      return type.asClassOrInterfaceType().getNameAsString();
    }
    return type.toString();
  }

  private Integer getStartLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().begin.line;
  }

  private Integer getEndLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().end.line;
  }
}
