package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.CallGraphResult;
import com.opensourcereader.core.analysis.dto.callgraph.RawMethodCall;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.OpensourceRepoContentMethodCallGraphService;
import com.opensourcereader.core.analysis.service.impl.callgraph.BuildArtifactCollector;
import com.opensourcereader.core.analysis.service.impl.callgraph.BuildExecutor;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphAnalyzer;
import com.opensourcereader.core.analysis.service.impl.callgraph.GitWorktreeManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpensourceRepoContentMethodCallGraphService
    implements OpensourceRepoContentMethodCallGraphService {

  private final GitWorktreeManager gitWorktreeManager;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final CallGraphAnalyzer callGraphAnalyzer;
  private final CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @Override
  @Transactional
  public void createMethodCallGraph(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManager.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);

    List<Path> byteCodeFiles = buildArtifactCollector.collectClassFiles(worktree);
    for (CallGraphResult callGraphResult : callGraphAnalyzer.analyzeByteCodeFiles(byteCodeFiles)) {
      Map<CodeMethodSignature, List<RawMethodCall>> rawCallsByCallerSignature =
          groupByCallerSignature(callGraphResult);
      List<CodeMethodMetaData> callers = resolveCallers(callGraphResult, rawCallsByCallerSignature);
      for (CodeMethodMetaData caller : callers) {
        caller.updateAllOutgoingCalls(
            createOutgoingCalls(
                caller, rawCallsByCallerSignature.get(caller.getMethodSignature())));
        caller.updateAllIngoingCalls(
            createIngoingCalls(callGraphResult.linkedInterfacePaths(), caller));
      }

      codeMethodMetaDataRepository.saveAll(callers);
    }
  }

  private Map<CodeMethodSignature, List<RawMethodCall>> groupByCallerSignature(
      CallGraphResult result) {
    return result.rawMethodCalls().stream()
        .collect(
            Collectors.groupingBy(
                call ->
                    CodeMethodSignature.of(
                        call.callerMethodName(), call.callerRawArgumentTypes())));
  }

  private List<CodeMethodMetaData> resolveCallers(
      CallGraphResult callGraphResult,
      Map<CodeMethodSignature, List<RawMethodCall>> rawCallsByCallerSignature) {
    return rawCallsByCallerSignature.keySet().stream()
        .map(
            codeMethodSignature ->
                codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                    callGraphResult.classPath(), codeMethodSignature.methodSignature()))
        .flatMap(Optional::stream)
        .toList();
  }

  private List<CodeMethodCallEdge> createOutgoingCalls(
      CodeMethodMetaData caller, List<RawMethodCall> calleeRawMethodCalls) {
    return calleeRawMethodCalls.stream()
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
