package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
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
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoClassMethodService implements OpenSourceRepoClassMethodService {

  private final CodeMethodRepository codeMethodRepository;

  @Override
  @Transactional
  public List<CodeMethod> createMethodCallGraph(List<MethodCallsOfClass> methodCallsOfClasses) {
    List<CodeMethod> entireCodeMethods = new ArrayList<>();
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
      entireCodeMethods.addAll(codeMethodRepository.saveAll(callers));
    }
    return entireCodeMethods;
  }

  // map으로 뽑기
  private Map<CodeMethodSignature, List<MethodCallEdge>> groupByCallerSignature(
      MethodCallsOfClass result) {
    return result.methodCallEdges().stream()
        .collect(
            Collectors.groupingBy(
                call ->
                    CodeMethodSignature.of(
                        call.caller().methodName(), call.caller().argumentTypes())));
  }

  // 클래스 메서드의 Caller를 가져옵니다
  private List<CodeMethod> resolveCallers(
      MethodCallsOfClass methodCallsOfClass,
      Map<CodeMethodSignature, List<MethodCallEdge>> rawCallsByCallerSignature) {
    return rawCallsByCallerSignature.keySet().stream()
        .map(
            codeMethodSignature ->
                codeMethodRepository.findByRepoContentPathAndMethodSignature(
                    methodCallsOfClass.classPath(), codeMethodSignature.methodSignature()))
        .flatMap(Optional::stream)
        .toList();
  }

  // graph 연결
  private List<CodeMethodCallEdge> createOutgoingCalls(
      CodeMethod caller, List<MethodCallEdge> calleeMethodCallEdges) {
    return calleeMethodCallEdges.stream()
        .map(
            call -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(call.callee().methodName(), call.callee().argumentTypes());
              return codeMethodRepository.findByRepoContentPathAndMethodSignature(
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
                codeMethodRepository.findByRepoContentPathAndMethodSignature(
                    path, caller.getMethodSignature().methodSignature()))
        .flatMap(Optional::stream)
        .map(ingoing -> new CodeMethodCallEdge(ingoing, caller))
        .toList();
  }
}
