package com.opensourcereader.core.analysis.infra.dto;

import java.util.Map;

import com.opensourcereader.core.analysis.entity.method.MethodSignature;

public record ParsedSourceFile(
    String typeInternalName, Map<MethodSignature, SourceCodeParseResult> methods) {
  public static ParsedSourceFile empty() {
    return new ParsedSourceFile(null, Map.of());
  }
}
