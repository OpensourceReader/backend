package com.opensourcereader.core.analysis.infra.bytecode;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.ClassBytecode;
import com.opensourcereader.core.analysis.infra.git.GitWorktreeManagerCli;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BytecodeClassStructureExtractor {

  private final GitWorktreeManagerCli gitWorktreeManagerCli;
  private final BuildExecutor buildExecutor;
  private final BuildArtifactCollector buildArtifactCollector;
  private final ClassStructureExtractor classStructureExtractor;

  public List<ByteCodeClassStructure> extract(
      Path savedLocalPath, String reference, String workingTreeDirName) {
    Path worktree =
        gitWorktreeManagerCli.createWorktree(savedLocalPath, reference, workingTreeDirName);
    buildExecutor.build(worktree);
    List<ClassBytecode> classBytecodes = buildArtifactCollector.collectClassFiles(worktree);

    return classStructureExtractor.extract(classBytecodes);
  }
}
