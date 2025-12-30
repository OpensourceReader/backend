package com.opensourcereader.api.dto;

import java.util.List;

import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;

public record CodeMethodResponse(
    Long id,
    String rawText,
    Integer startLine,
    Integer endLine,
    List<CodeMethodSummary> ingoing,
    List<CodeMethodSummary> outgoing) {

  public static CodeMethodResponse of(CodeMethod codeMethod) {
    return new CodeMethodResponse(
        codeMethod.getId(),
        codeMethod.getOpenSourceRepoContent().getRawText(),
        codeMethod.getStartLine(),
        codeMethod.getEndLine(),
        CodeMethodSummary.ingoing(codeMethod.getIngoingCalls()),
        CodeMethodSummary.outgoing(codeMethod.getOutgoingCalls()));
  }
}
