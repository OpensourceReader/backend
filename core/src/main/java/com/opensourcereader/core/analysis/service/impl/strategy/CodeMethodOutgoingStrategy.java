package com.opensourcereader.core.analysis.service.impl.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeMethodOutgoingStrategy {

  private final CodeMethodRepository codeMethodRepository;

  public List<CodeMethodCallEdge> getOutgoings(
      Long repoId, CodeMethod caller, List<MethodCallInfo> calleeMethods) {
    return calleeMethods.stream()
        .map(methodCallInfo -> resolveOrCreateCallee(repoId, methodCallInfo))
        .map(callee -> CodeMethodCallEdge.of(caller, callee))
        .toList();
  }

  private CodeMethod resolveOrCreateCallee(Long repoId, MethodCallInfo callee) {
    CodeMethodSignature signature =
        CodeMethodSignature.of(
            callee.methodName(),
            callee.descriptor().argumentTypes(),
            callee.descriptor().methodReturnType());
    return codeMethodRepository
        .findByRepoIdAndClassInternalNameAndMethodSignature(
            repoId, callee.className(), signature.methodSignature())
        .orElseGet(() -> CodeMethod.external(callee, signature));
  }
}
