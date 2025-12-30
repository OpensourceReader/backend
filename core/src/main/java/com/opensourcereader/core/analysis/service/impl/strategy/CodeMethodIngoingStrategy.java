package com.opensourcereader.core.analysis.service.impl.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeMethodIngoingStrategy {

  private final CodeMethodRepository codeMethodRepository;

  public List<CodeMethodCallEdge> getIngoing(
      Long repoId, List<String> interfaceNames, CodeMethod caller) {
    return interfaceNames.stream()
        .map(interfaceName -> resolveOrCreateIngoing(repoId, interfaceName, caller))
        .map(ingoing -> CodeMethodCallEdge.of(ingoing, caller))
        .toList();
  }

  private CodeMethod resolveOrCreateIngoing(Long repoId, String interfaceName, CodeMethod caller) {
    return codeMethodRepository
        .findByRepoIdAndClassInternalNameAndMethodSignature(
            repoId, interfaceName, caller.getMethodSignature().methodSignature())
        .orElseGet(() -> CodeMethod.external(interfaceName, caller));
  }
}
