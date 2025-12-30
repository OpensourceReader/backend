package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;
import com.opensourcereader.core.analysis.service.impl.strategy.CodeMethodIngoingStrategy;
import com.opensourcereader.core.analysis.service.impl.strategy.CodeMethodOutgoingStrategy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoClassMethodService implements OpenSourceRepoClassMethodService {

  private final CodeMethodRepository codeMethodRepository;
  private final CodeMethodOutgoingStrategy codeMethodOutgoingStrategy;
  private final CodeMethodIngoingStrategy codeMethodIngoingStrategy;

  @Override
  @Transactional
  public List<CodeMethod> createMethodCallGraph(Long repoId, List<ClassStructure> classStructures) {
    List<CodeMethod> entireCodeMethods = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      Map<CodeMethodSignature, MethodStructure> methods =
          getByMethodSignature(classStructure.methods());

      List<CodeMethod> callers = resolveCallers(repoId, classStructure.methods());
      callers.forEach(
          caller -> {
            List<CodeMethodCallEdge> outgoing =
                codeMethodOutgoingStrategy.getOutgoings(
                    repoId, caller, methods.get(caller.getMethodSignature()).calleeMethods());
            List<CodeMethodCallEdge> ingoing =
                codeMethodIngoingStrategy.getIngoing(
                    repoId, classStructure.classInfo().interfaceNames(), caller);
            caller.updateAllCalls(outgoing, ingoing);
          });
      entireCodeMethods.addAll(codeMethodRepository.saveAll(callers));
    }
    return entireCodeMethods;
  }

  private Map<CodeMethodSignature, MethodStructure> getByMethodSignature(
      List<MethodStructure> methods) {
    return methods.stream()
        .collect(
            Collectors.toMap(cms -> CodeMethodSignature.of(cms.declaredMethodInfo()), cms -> cms));
  }

  private List<CodeMethod> resolveCallers(Long repoId, List<MethodStructure> methods) {
    return methods.stream()
        .map(method -> findCodeMethod(repoId, method))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<CodeMethod> findCodeMethod(Long repoId, MethodStructure methodStructure) {
    CodeMethodSignature codeMethodSignature =
        CodeMethodSignature.of(methodStructure.declaredMethodInfo());
    return codeMethodRepository.findByRepoIdAndClassInternalNameAndMethodSignature(
        repoId,
        methodStructure.declaredMethodInfo().className(),
        codeMethodSignature.methodSignature());
  }
}
