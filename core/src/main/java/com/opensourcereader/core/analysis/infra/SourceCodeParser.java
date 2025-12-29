package com.opensourcereader.core.analysis.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.Type;
import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.entity.codedetail.AccessModifier;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SourceCodeParser {

  private static final String VAR_ARGS_EXPRESSION = "...";
  private final JavaParser javaParser;

  public String extractClassName(String rawText) {
    ParseResult<CompilationUnit> result = javaParser.parse(rawText);
    if (!result.isSuccessful() || result.getResult().isEmpty()) {
      return "";
    }

    CompilationUnit cu = result.getResult().get();

    String pkg = cu.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("");

    for (TypeDeclaration<?> t : cu.findAll(TypeDeclaration.class)) {
      if (!t.isTopLevelType()) continue;
      String className = t.getNameAsString();
      if (pkg.isEmpty()) {
        return className;
      } else {
        return pkg + "." + className;
      }
    }

    return "";
  }

  public List<SourceCodeParseResult> extractCodeMethods(String rawText) {
    ParseResult<CompilationUnit> compilationUnit = javaParser.parse(rawText);
    if (!compilationUnit.isSuccessful() || compilationUnit.getResult().isEmpty()) {
      return List.of();
    }
    return compilationUnit.getResult().get().findAll(MethodDeclaration.class).stream()
        .map(
            methodDeclaration ->
                new SourceCodeParseResult(
                    methodDeclaration.getNameAsString(),
                    AccessModifier.from(methodDeclaration),
                    getParameterTypes(methodDeclaration),
                    getStartLine(methodDeclaration),
                    getEndLine(methodDeclaration)))
        .toList();
  }

  private List<String> getParameterTypes(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getParameters().stream().map(this::getParameterType).toList();
  }

  private String getParameterType(Parameter methodParameter) {
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
