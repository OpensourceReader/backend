package com.opensourcereader.api.dto;

import java.util.List;

public record CodeMethodResponse(
    Long id,
    String classInternalName,
    String methodName,
    String rawText,
    Integer startLine,
    Integer endLine,
    List<CodeMethodSummary> ingoing,
    List<CodeMethodSummary> outgoing) {}
