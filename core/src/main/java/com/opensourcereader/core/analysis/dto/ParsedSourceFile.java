package com.opensourcereader.core.analysis.dto;

import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import java.util.Map;

public record ParsedSourceFile(
    String classInternalName,
    Map<CodeMethodSignature, SourceCodeParseResult> methodsBySignature
) {
  public static ParsedSourceFile empty() {
    return new ParsedSourceFile(null, Map.of());
  }
}