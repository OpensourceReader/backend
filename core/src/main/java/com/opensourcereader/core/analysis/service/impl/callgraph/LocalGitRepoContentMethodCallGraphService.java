package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphAnalyzer.CallGraphResult;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphClassVisitor.CalleePathAndMethodDescriptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalGitRepoContentMethodCallGraphService {

  private final GitWorktreeManager gitWorktreeManager;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final CallGraphAnalyzer callGraphAnalyzer;
  private final CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @Transactional
  public boolean createMethodCallGraph(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManager.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);

    for (Path byteCodeFile : buildArtifactCollector.collectClassFiles(worktree)) {
      CallGraphResult callGraphResult =
          callGraphAnalyzer.analyzeByteCodeFile(byteCodeFile); // 한번에 다 가져다 주기
      for (Map.Entry<String, Set<CalleePathAndMethodDescriptor>> edge :
          callGraphResult.edges().entrySet()) {
        Optional<CodeMethodMetaData> codeMethodMetaData =
            codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
                callGraphResult.classInternalName() + ".java", edge.getKey()); // .java 수정 필요
        if (codeMethodMetaData.isEmpty()) {
          continue;
        }
        CodeMethodMetaData caller = codeMethodMetaData.get();
        caller.updateAllOutgoingCalls(getOutgoingCalls(edge, caller));
        caller.updateAllIngoingCalls(getIngoingCalls(callGraphResult, caller));

        codeMethodMetaDataRepository.save(caller);
      }
    }

    return true;
  }

  private List<CodeMethodCallEdge> getIngoingCalls(
      CallGraphResult callGraphResult, CodeMethodMetaData caller) {
    List<CodeMethodCallEdge> methodCallEdges = new ArrayList<>();
    for (String linkedInterface : callGraphResult.interfaces()) {
      Optional<CodeMethodMetaData> ingoingCodeMethodMetaData =
          codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
              linkedInterface + ".java", caller.getMethodSignature());
      if (ingoingCodeMethodMetaData.isEmpty()) {
        continue;
      }
      methodCallEdges.add(new CodeMethodCallEdge(ingoingCodeMethodMetaData.get(), caller));
    }
    return methodCallEdges;
  }

  private List<CodeMethodCallEdge> getOutgoingCalls(
      Map.Entry<String, Set<CalleePathAndMethodDescriptor>> edge, CodeMethodMetaData caller) {
    List<CodeMethodCallEdge> methodCallEdges = new ArrayList<>();
    for (CalleePathAndMethodDescriptor calleeInfo : edge.getValue()) {
      Optional<CodeMethodMetaData> CodeMethodMetaData =
          codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(
              calleeInfo.calleePath() + ".java", calleeInfo.methodDescriptor());
      if (CodeMethodMetaData.isEmpty()) {
        continue;
      }
      methodCallEdges.add(new CodeMethodCallEdge(caller, CodeMethodMetaData.get()));
    }
    return methodCallEdges;
  }
}
