package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.Extension;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodSignature;
import com.opensourcereader.core.analysis.infra.SourceCodeParser;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceCodeMethodExtractor {

  private final SourceCodeParser sourceCodeParser;

  public List<CodeMethodExtractResult> extract(
      OpenSourceFileInfo sourceFile, ClassStructure classStructure) {
    if (!Extension.isJavaFile(sourceFile.path()) || classStructure == null) {
      return List.of();
    }
    Map<CodeMethodSignature, SourceCodeParseResult> sourceCodeMethods =
        getByMethodSignature(sourceFile.rawText());

    return classStructure.methods().stream()
        .map(method -> getCodeMethodExtractResult(method, sourceCodeMethods))
        .toList();
  }

  private Map<CodeMethodSignature, SourceCodeParseResult> getByMethodSignature(String rawText) {
    return sourceCodeParser.extractCodeMethods(rawText).stream()
        .collect(
            Collectors.toMap(
                result -> CodeMethodSignature.of(result.methodName(), result.paramTypes()),
                extractResult -> extractResult));
  }

  private CodeMethodExtractResult getCodeMethodExtractResult(
      MethodStructure method, Map<CodeMethodSignature, SourceCodeParseResult> sourceCodeMethods) {
    CodeMethodSignature methodSignature = CodeMethodSignature.of(method.declaredMethodInfo());
    return CodeMethodExtractResult.of(
        method.declaredMethodInfo(), sourceCodeMethods.get(methodSignature));
  }
}
