package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
  public boolean createMethodCallGraph(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManager.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);

    // 바이트 코드 하나씩
    for (Path byteCodeFile : buildArtifactCollector.collectClassFiles(worktree)) {
      CallGraphResult callGraphResult = callGraphAnalyzer.analyzeByteCodeFile(byteCodeFile);
      Map<CodeMethodSignature, List<RawMethodCall>> rawCallsByCallerSignature =
          callGraphResult.rawMethodCalls().stream()
              .collect(
                  Collectors.groupingBy(
                      rawMethodCall ->
                          CodeMethodSignature.of(
                              rawMethodCall.callerMethodName(),
                              rawMethodCall.callerRawArgumentTypes())));

      for (Entry<CodeMethodSignature, List<RawMethodCall>> rawCallByCallerSignature :
          rawCallsByCallerSignature.entrySet()) {
        Optional<CodeMethodMetaData> callerCodeMethodMetaData =
            codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                callGraphResult.classPath(), rawCallByCallerSignature.getKey().methodSignature());
        if (callerCodeMethodMetaData.isEmpty()) {
          continue;
        }
        CodeMethodMetaData caller = callerCodeMethodMetaData.get();
        caller.updateAllOutgoingCalls(
            getOutgoingCalls(caller, rawCallByCallerSignature.getValue()));
        caller.updateAllIngoingCalls(
            getIngoingCalls(callGraphResult.linkedInterfacePaths(), caller));

        codeMethodMetaDataRepository.save(caller);
      }
    }

    return true;
  }

  private List<CodeMethodCallEdge> getOutgoingCalls(
      CodeMethodMetaData caller, List<RawMethodCall> calleeRawMethodCalls) {
    List<CodeMethodCallEdge> methodCallEdges = new ArrayList<>();
    for (RawMethodCall rawMethodCall : calleeRawMethodCalls) {
      CodeMethodSignature calleeMethodSignature =
          CodeMethodSignature.of(
              rawMethodCall.calleeMethodName(), rawMethodCall.calleeRawArgumentTypes());
      Optional<CodeMethodMetaData> CodeMethodMetaData =
          codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
              rawMethodCall.calleeClassPath(), calleeMethodSignature.methodSignature());
      if (CodeMethodMetaData.isEmpty()) {
        continue;
      }
      methodCallEdges.add(new CodeMethodCallEdge(caller, CodeMethodMetaData.get()));
    }
    return methodCallEdges;
  }

  private List<CodeMethodCallEdge> getIngoingCalls(
      List<String> linkedInterfacePaths, CodeMethodMetaData caller) {
    List<CodeMethodCallEdge> methodCallEdges = new ArrayList<>();
    for (String linkedInterfacePath : linkedInterfacePaths) {
      Optional<CodeMethodMetaData> ingoingCodeMethodMetaData =
          codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
              linkedInterfacePath, caller.getMethodSignature().methodSignature());
      if (ingoingCodeMethodMetaData.isEmpty()) {
        continue;
      }
      methodCallEdges.add(new CodeMethodCallEdge(ingoingCodeMethodMetaData.get(), caller));
    }
    return methodCallEdges;
  }
}
