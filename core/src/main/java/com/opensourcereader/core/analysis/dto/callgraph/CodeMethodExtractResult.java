package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.entity.codedetail.AccessModifier;
import com.opensourcereader.core.analysis.entity.codedetail.NonAccessModifier;

public record CodeMethodExtractResult(
    String methodName,
    AccessModifier modifier,
    EnumSet<NonAccessModifier> nonAccessModifiers,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {

  public static CodeMethodExtractResult of(
      DeclaredMethodInfo declaredMethodInfo, SourceCodeParseResult codeParseResult) {
    return new CodeMethodExtractResult(
        declaredMethodInfo.methodName(),
        declaredMethodInfo.accessModifier(),
        declaredMethodInfo.nonAccessModifiers(),
        declaredMethodInfo.methodDescriptor().argumentTypes(),
        codeParseResult.startLine(),
        codeParseResult.endLine());
  }
}
