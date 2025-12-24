package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.opensourcereader.core.analysis.dto.OpenSourceContentMethodExtractResult;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContentName;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceMethodExtractor {

  private final JavaParser javaParser;

  public List<OpenSourceContentMethodExtractResult> separateCodeMethods(
      OpenSourceFileInfo openSourceFileInfo) {
    if (OpenSourceRepoContentName.isNotJavaFile(openSourceFileInfo.path())) {
      return List.of();
    }
    ParseResult<CompilationUnit> compilationUnit = javaParser.parse(openSourceFileInfo.rawText());
    if (!compilationUnit.isSuccessful() || compilationUnit.getResult().isEmpty()) {
      return List.of();
    }
    return compilationUnit.getResult().get().findAll(MethodDeclaration.class).stream()
        .map(OpenSourceContentMethodExtractResult::of)
        .toList();
  }
}
