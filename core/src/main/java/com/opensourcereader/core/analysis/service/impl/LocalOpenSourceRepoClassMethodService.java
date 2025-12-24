package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCall;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
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
  public void createMethodCallGraph(List<ClassMethodCallResult> classMethodCallResults) {
    for (ClassMethodCallResult classMethodCallResult : classMethodCallResults) {
      Map<CodeMethodSignature, List<MethodCall>> rawCallsByCallerSignature =
          groupByCallerSignature(classMethodCallResult);
      List<CodeMethodMetaData> callers =
          resolveCallers(classMethodCallResult, rawCallsByCallerSignature);
      for (CodeMethodMetaData caller : callers) {
        caller.updateAllOutgoingCalls(
            createOutgoingCalls(
                caller, rawCallsByCallerSignature.get(caller.getMethodSignature())));
        caller.updateAllIngoingCalls(
            createIngoingCalls(classMethodCallResult.linkedInterfacePaths(), caller));
      }

      codeMethodMetaDataRepository.saveAll(callers);
    }
  }

  private Map<CodeMethodSignature, List<MethodCall>> groupByCallerSignature(
      ClassMethodCallResult result) {
    return result.methodCalls().stream()
        .collect(
            Collectors.groupingBy(
                call ->
                    CodeMethodSignature.of(
                        call.callerMethodName(), call.callerRawArgumentTypes())));
  }

  private List<CodeMethodMetaData> resolveCallers(
      ClassMethodCallResult classMethodCallResult,
      Map<CodeMethodSignature, List<MethodCall>> rawCallsByCallerSignature) {
    return rawCallsByCallerSignature.keySet().stream()
        .map(
            codeMethodSignature ->
                codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                    classMethodCallResult.classPath(), codeMethodSignature.methodSignature()))
        .flatMap(Optional::stream)
        .toList();
  }

  private List<CodeMethodCallEdge> createOutgoingCalls(
      CodeMethodMetaData caller, List<MethodCall> calleeMethodCalls) {
    return calleeMethodCalls.stream()
        .map(
            call -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(call.calleeMethodName(), call.calleeRawArgumentTypes());
              return codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                  call.calleeClassPath(), codeMethodSignature.methodSignature());
            })
        .flatMap(Optional::stream)
        .map(outgoing -> new CodeMethodCallEdge(caller, outgoing))
        .toList();
  }

  private List<CodeMethodCallEdge> createIngoingCalls(
      List<String> linkedInterfacePaths, CodeMethodMetaData caller) {
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
