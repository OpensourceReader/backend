package com.opensourcereader.api.facade.analysis;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.api.dto.CodeMethodResponse;
import com.opensourcereader.api.dto.CodeMethodSummary;
import com.opensourcereader.api.viewpolicy.CodeMethodViewPolicy;
import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceCodeMethodFacade {

  private final CodeMethodCallGraphService codeMethodCallGraphService;
  private final CodeMethodViewPolicy codeMethodViewPolicy;

  public CodeMethodResponse getCodeMethodById(CodeMethodRequest request) {
    Method method = codeMethodCallGraphService.getCodeMethodById(request.codeMethodId());

    return new CodeMethodResponse(
        method.getId(),
        method.getTypeInternalName(),
        method.getMethodName(),
        extractRawText(method),
        method.getStartLine(),
        method.getEndLine(),
        filterIngoing(method, request),
        filterOutgoing(method, request));
  }

  private List<CodeMethodSummary> filterIngoing(Method method, CodeMethodRequest request) {
    return method.getIngoingCalls().stream()
        .map(MethodCallEdge::getCaller)
        .filter(caller -> codeMethodViewPolicy.isVisible(caller, request))
        .map(CodeMethodSummary::from)
        .toList();
  }

  private List<CodeMethodSummary> filterOutgoing(Method method, CodeMethodRequest request) {
    return method.getOutgoingCalls().stream()
        .map(MethodCallEdge::getCallee)
        .filter(callee -> codeMethodViewPolicy.isVisible(callee, request))
        .map(CodeMethodSummary::from)
        .toList();
  }

  private String extractRawText(Method method) {
    if (method.getDeclaredType().getOpenSourceRepoContent() == null) {
      return null;
    }
    return method.getDeclaredType().getOpenSourceRepoContent().getRawText();
  }
}
