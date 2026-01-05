package com.opensourcereader.core.analysis.dto;

import java.util.Map;

import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;

public record ParsedSourceFile(
    String classInternalName, Map<CodeMethodSignature, SourceCodeParseResult> methodsBySignature) {
  public static ParsedSourceFile empty() {
    return new ParsedSourceFile(null, Map.of());
  }
}
