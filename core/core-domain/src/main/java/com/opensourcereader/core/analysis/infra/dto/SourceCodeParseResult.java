package com.opensourcereader.core.analysis.infra.dto;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.method.MethodModifier;

public record SourceCodeParseResult(
    String methodName,
    EnumSet<MethodModifier> modifier,
    String returnType,
    List<String> argumentTypes,
    Integer startLine,
    Integer endLine) {}
