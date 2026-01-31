package com.opensourcereader.core.analysis.dto;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;

public record MethodInfo(
    String className,
    String methodName,
    EnumSet<MethodModifier> methodModifiers,
    MethodDescriptor methodDescriptor,
    String genericSignature,
    List<String> exceptions,
    Integer startLine,
    Integer endLine) {

  public static MethodInfo of(
      ByteCodeDeclaredMethodInfo byteCodeDeclaredMethodInfo,
      SourceCodeParseResult codeParseResult) {
    return new MethodInfo(
        byteCodeDeclaredMethodInfo.className(),
        byteCodeDeclaredMethodInfo.methodName(),
        byteCodeDeclaredMethodInfo.methodModifiers(),
        byteCodeDeclaredMethodInfo.methodDescriptor(),
        byteCodeDeclaredMethodInfo.genericSignature(),
        byteCodeDeclaredMethodInfo.exceptions(),
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
