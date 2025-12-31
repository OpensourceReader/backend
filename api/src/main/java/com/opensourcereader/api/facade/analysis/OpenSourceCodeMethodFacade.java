package com.opensourcereader.api.facade.analysis;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.api.dto.CodeMethodResponse;
import com.opensourcereader.api.dto.CodeMethodSummary;
import com.opensourcereader.api.facade.analysis.viewpolicy.CodeMethodViewPolicy;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.service.CodeMethodGraphQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceCodeMethodFacade {

  private final CodeMethodGraphQueryService codeMethodGraphQueryService;
  private final CodeMethodViewPolicy codeMethodViewPolicy;

  public CodeMethodResponse getCodeMethodById(CodeMethodRequest request) {
    CodeMethod method = codeMethodGraphQueryService.getCodeMethodById(request.codeMethodId());

    return new CodeMethodResponse(
        method.getId(),
        method.getClassInternalName(),
        method.getMethodName(),
        extractRawText(method),
        method.getStartLine(),
        method.getEndLine(),
        filterIngoing(method, request),
        filterOutgoing(method, request));
  }

  private List<CodeMethodSummary> filterIngoing(CodeMethod method, CodeMethodRequest request) {
    return method.getIngoingCalls().stream()
        .map(CodeMethodCallEdge::getCaller)
        .filter(caller -> codeMethodViewPolicy.isVisible(caller, request))
        .map(CodeMethodSummary::from)
        .toList();
  }

  private List<CodeMethodSummary> filterOutgoing(CodeMethod method, CodeMethodRequest request) {
    return method.getOutgoingCalls().stream()
        .map(CodeMethodCallEdge::getCallee)
        .filter(callee -> codeMethodViewPolicy.isVisible(callee, request))
        .map(CodeMethodSummary::from)
        .toList();
  }

  private String extractRawText(CodeMethod method) {
    if (method.getOpenSourceRepoContent() == null) {
      return null;
    }
    return method.getOpenSourceRepoContent().getRawText();
  }
}
