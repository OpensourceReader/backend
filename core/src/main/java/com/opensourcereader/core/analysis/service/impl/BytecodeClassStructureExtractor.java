package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.infra.bytecode.BuildArtifactCollector;
import com.opensourcereader.core.analysis.infra.bytecode.BuildExecutor;
import com.opensourcereader.core.analysis.infra.bytecode.ClassStructureExtractor;
import com.opensourcereader.core.analysis.infra.git.GitWorktreeManagerCli;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassStructureExtractor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BytecodeClassStructureExtractor implements OpenSourceRepoClassStructureExtractor {

  private final GitWorktreeManagerCli gitWorktreeManagerCli;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final ClassStructureExtractor classStructureExtractor;

  @Override
  public List<ClassStructure> createClassStructures(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManagerCli.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);
    List<ClassBytecode> classBytecodes = buildArtifactCollector.collectClassFiles(worktree);

    return classStructureExtractor.extract(classBytecodes);
  }
}
