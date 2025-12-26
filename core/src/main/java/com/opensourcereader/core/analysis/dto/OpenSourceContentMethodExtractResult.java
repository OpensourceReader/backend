package com.opensourcereader.core.analysis.dto;

import java.util.List;

import com.opensourcereader.core.analysis.entity.codedetail.MethodAccessModifier;

public record OpenSourceContentMethodExtractResult(
    String methodName,
    MethodAccessModifier modifier,
    List<String> paramTypes,
    Integer startLine,
    Integer endLine) {}
