package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.List;

import com.opensourcereader.core.analysis.entity.method.AccessModifier;

public record SourceCodeParseResult(
    String methodName,
    AccessModifier modifier,
    String returnType,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {}
