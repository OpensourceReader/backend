package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.List;

import com.opensourcereader.core.analysis.entity.codedetail.AccessModifier;

public record SourceCodeParseResult(
    String methodName,
    AccessModifier modifier,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {}
