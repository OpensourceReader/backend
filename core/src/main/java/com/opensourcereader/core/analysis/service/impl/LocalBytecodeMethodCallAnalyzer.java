package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;
import com.opensourcereader.core.analysis.service.OpenSourceRepoMethodCallAnalyzer;
import com.opensourcereader.core.analysis.service.impl.callgraph.BuildArtifactCollector;
import com.opensourcereader.core.analysis.service.impl.callgraph.BuildExecutor;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphAnalyzer;
import com.opensourcereader.core.analysis.service.impl.callgraph.GitWorktreeManagerCli;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocalBytecodeMethodCallAnalyzer implements OpenSourceRepoMethodCallAnalyzer {

  private final GitWorktreeManagerCli gitWorktreeManagerCli;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final CallGraphAnalyzer callGraphAnalyzer;

  @Override
  public List<ClassMethodCallResult> createMethodCallResults(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManagerCli.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);
    List<Path> byteCodeFiles = buildArtifactCollector.collectClassFiles(worktree);

    return callGraphAnalyzer.analyzeByteCodeFiles(byteCodeFiles);
  }
}
