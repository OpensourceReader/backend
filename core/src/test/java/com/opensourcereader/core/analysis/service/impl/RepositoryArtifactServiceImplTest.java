package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.RepositoryArtifact;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent.RepoEntryType;
import com.opensourcereader.core.analysis.infra.bytecode.BytecodeClassStructureExtractor;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.gitrepo.GitRepositoryLoadResult;
import com.opensourcereader.core.analysis.infra.git.GitRepositoryLoader;
import com.opensourcereader.core.analysis.infra.parser.SourceFileParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryArtifactServiceImplTest {

  @Mock private GitRepositoryLoader gitRepositoryLoader;
  @Mock private BytecodeClassStructureExtractor byteCodeStructureExtractor;
  @Mock private SourceFileParser sourceFileParser;

  @InjectMocks private RepositoryArtifactServiceImpl repositoryArtifactsProducer;

  @Test
  @DisplayName("produce(): git repo를 다운로드하고, 결과 savedLocalRepoPath를 RepositoryArtifact에 담아 반환한다")
  void create_returnsArtifactWithSavedLocalRepoPath() {
    // given
    Path savedLocalPath = Path.of("/tmp/repo");
    GitRepositoryLoadResult loadResult = new GitRepositoryLoadResult(savedLocalPath, List.of());

    given(gitRepositoryLoader.downloadGitRepo(any(), any(), any())).willReturn(loadResult);
    given(byteCodeStructureExtractor.extract(any(), any(), any())).willReturn(List.of());

    // when
    RepositoryArtifact artifact =
        repositoryArtifactsProducer.create("uri", "ref", "/clone", "workdir");

    // then
    assertThat(artifact.savedLocalRepoPath()).isEqualTo(savedLocalPath);
    assertThat(artifact.typeStructures()).isEmpty();

    verify(gitRepositoryLoader).downloadGitRepo("uri", "ref", "/clone");
    verify(byteCodeStructureExtractor).extract(savedLocalPath, "ref", "workdir");
  }

  @Test
  @DisplayName("getStructureWithSources(): supported가 아닌 파일은 TypeStructure 생성 대상에서 제외한다")
  void create_filtersUnsupportedFiles() {
    // given
    Path savedLocalPath = Path.of("/tmp/repo");
    OpenSourceFileInfo unsupportedFile =
        new OpenSourceFileInfo(savedLocalPath.toString(), RepoEntryType.OTHERS, "");
    GitRepositoryLoadResult loadResult =
        new GitRepositoryLoadResult(savedLocalPath, List.of(unsupportedFile));

    given(gitRepositoryLoader.downloadGitRepo(any(), any(), any())).willReturn(loadResult);

    given(byteCodeStructureExtractor.extract(any(), any(), any())).willReturn(List.of());

    // when
    RepositoryArtifact artifact =
        repositoryArtifactsProducer.create("uri", "ref", "/clone", "workdir");

    // then
    assertThat(artifact.typeStructures()).isEmpty();

    // supported가 아니면 parse도 호출되면 안 됨
    verifyNoInteractions(sourceFileParser);
  }
}
