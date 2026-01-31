package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.RepositoryArtifact;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.infra.bytecode.BytecodeClassStructureExtractor;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.gitrepo.GitRepositoryLoadResult;
import com.opensourcereader.core.analysis.infra.git.GitRepositoryLoader;
import com.opensourcereader.core.analysis.infra.parser.SourceFileParser;
import com.opensourcereader.core.analysis.service.RepositoryArtifactService;
import com.opensourcereader.core.analysis.service.impl.detail.TypeStructureResolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RepositoryArtifactServiceImpl implements RepositoryArtifactService {

  private final GitRepositoryLoader gitRepositoryLoader;
  private final BytecodeClassStructureExtractor bytecodeClassStructureExtractor;
  private final SourceFileParser sourceFileParser;
  private final TypeStructureResolver typeStructureResolver;

  @Override
  public RepositoryArtifact create(
      String openSourceUri, String reference, String localClonePath, String workingTreeDirName) {
    GitRepositoryLoadResult gitRepoLoadResult =
        gitRepositoryLoader.downloadGitRepo(openSourceUri, reference, localClonePath);
    List<TypeStructure> structureWithSources =
        createTypeStructures(gitRepoLoadResult, reference, workingTreeDirName);

    return new RepositoryArtifact(gitRepoLoadResult.savedLocalRepoPath(), structureWithSources);
  }

  private List<TypeStructure> createTypeStructures(
      GitRepositoryLoadResult gitRepoLoadResult, String reference, String workingTreeDirName) {
    Map<String, ByteCodeClassStructure> byteCodeStructures =
        bytecodeClassStructureExtractor
            .extract(gitRepoLoadResult.savedLocalRepoPath(), reference, workingTreeDirName)
            .stream()
            .collect(Collectors.toMap(bcs -> bcs.typeInfo().internalName(), bcs -> bcs));

    return gitRepoLoadResult.files().stream()
        .filter(sourceFile -> sourceFile.repoEntryType().isSupported())
        .map(
            sourceFile ->
                typeStructureResolver.resolve(
                    sourceFile, sourceFileParser.parse(sourceFile), byteCodeStructures))
        .toList();
  }
}
