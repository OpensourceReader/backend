package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.entity.codemethod.AccessModifier;
import com.opensourcereader.core.analysis.entity.codemethod.NonAccessModifier;

public record CodeMethodExtractResult(
    String methodName,
    AccessModifier modifier,
    EnumSet<NonAccessModifier> nonAccessModifiers,
    String returnType,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {

  public static CodeMethodExtractResult of(
      DeclaredMethodInfo declaredMethodInfo, SourceCodeParseResult codeParseResult) {
    return new CodeMethodExtractResult(
        declaredMethodInfo.methodName(),
        declaredMethodInfo.accessModifier(),
        declaredMethodInfo.nonAccessModifiers(),
        declaredMethodInfo.methodDescriptor().methodReturnType(),
        declaredMethodInfo.methodDescriptor().argumentTypes(),
        getStartLine(codeParseResult),
        getEndLine(codeParseResult));
  }

  private static Integer getStartLine(SourceCodeParseResult codeParseResult) {
    if (codeParseResult == null) {
      return null;
    }
    return codeParseResult.startLine();
  }

  private static Integer getEndLine(SourceCodeParseResult codeParseResult) {
    if (codeParseResult == null) {
      return null;
    }
    return codeParseResult.endLine();
  }
}
