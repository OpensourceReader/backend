package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;
import com.opensourcereader.core.analysis.service.OpenSourceRepoMethodCallAnalyzer;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphAnalyzer;
import com.opensourcereader.core.analysis.service.impl.support.bytecode.BuildArtifactCollector;
import com.opensourcereader.core.analysis.service.impl.support.bytecode.BuildExecutor;
import com.opensourcereader.core.analysis.service.impl.support.git.GitWorktreeManagerCli;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocalBytecodeMethodCallAnalyzer implements OpenSourceRepoMethodCallAnalyzer {

  private final GitWorktreeManagerCli gitWorktreeManagerCli;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final CallGraphAnalyzer callGraphAnalyzer;

  @Override
  public List<ClassMethodCallResult> createClassMethodCalls(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManagerCli.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);
    List<Path> byteCodeFiles = buildArtifactCollector.collectClassFiles(worktree);

    return callGraphAnalyzer.analyzeByteCodeFiles(byteCodeFiles);
  }
}
