package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.MethodCallEdge;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethod;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoClassMethodService implements OpenSourceRepoClassMethodService {

  private final CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @Override
  @Transactional
  public void createMethodCallGraph(List<MethodCallsOfClass> methodCallsOfClasses) {
    for (MethodCallsOfClass methodCallsOfClass : methodCallsOfClasses) {
      Map<CodeMethodSignature, List<MethodCallEdge>> rawCallsByCallerSignature =
          groupByCallerSignature(methodCallsOfClass);
      List<CodeMethod> callers = resolveCallers(methodCallsOfClass, rawCallsByCallerSignature);
      for (CodeMethod caller : callers) {
        caller.updateAllOutgoingCalls(
            createOutgoingCalls(
                caller, rawCallsByCallerSignature.get(caller.getMethodSignature())));
        caller.updateAllIngoingCalls(
            createIngoingCalls(methodCallsOfClass.linkedInterfacePaths(), caller));
      }

      codeMethodMetaDataRepository.saveAll(callers);
    }
  }

  private Map<CodeMethodSignature, List<MethodCallEdge>> groupByCallerSignature(
      MethodCallsOfClass result) {
    return result.methodCallEdges().stream()
        .collect(
            Collectors.groupingBy(
                call ->
                    CodeMethodSignature.of(
                        call.caller().methodName(), call.caller().argumentTypes())));
  }

  private List<CodeMethod> resolveCallers(
      MethodCallsOfClass methodCallsOfClass,
      Map<CodeMethodSignature, List<MethodCallEdge>> rawCallsByCallerSignature) {
    return rawCallsByCallerSignature.keySet().stream()
        .map(
            codeMethodSignature ->
                codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                    methodCallsOfClass.classPath(), codeMethodSignature.methodSignature()))
        .flatMap(Optional::stream)
        .toList();
  }

  private List<CodeMethodCallEdge> createOutgoingCalls(
      CodeMethod caller, List<MethodCallEdge> calleeMethodCallEdges) {
    return calleeMethodCallEdges.stream()
        .map(
            call -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(call.callee().methodName(), call.callee().argumentTypes());
              return codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                  call.callee().classPath(), codeMethodSignature.methodSignature());
            })
        .flatMap(Optional::stream)
        .map(outgoing -> new CodeMethodCallEdge(caller, outgoing))
        .toList();
  }

  private List<CodeMethodCallEdge> createIngoingCalls(
      List<String> linkedInterfacePaths, CodeMethod caller) {
    return linkedInterfacePaths.stream()
        .map(
            path ->
                codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                    path, caller.getMethodSignature().methodSignature()))
        .flatMap(Optional::stream)
        .map(ingoing -> new CodeMethodCallEdge(ingoing, caller))
        .toList();
  }
}
