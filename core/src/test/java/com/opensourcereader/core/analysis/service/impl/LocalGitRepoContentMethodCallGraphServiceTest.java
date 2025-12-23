package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.impl.callgraph.LocalGitRepoContentMethodCallGraphService;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SpringBootTest
class LocalGitRepoContentMethodCallGraphServiceTest {

  @Autowired private GitRepositoryService gitRepositoryService;
  @Autowired private OpenSourceRepoService openSourceRepoService;

  @Autowired
  private LocalGitRepoContentMethodCallGraphService localGitRepoContentMethodCallGraphService;

  @Autowired private CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @TempDir private Path tempDir;

  private Path bareCloneRepoPath;
  private Path bareRepoWorkingTreePath;
  private Path bareRepoByteCodePath;

  @BeforeEach
  void setup() {
    bareCloneRepoPath = tempDir.resolve("repos");
    bareRepoWorkingTreePath = tempDir.resolve("repos/trees");
    bareRepoByteCodePath = tempDir.resolve("repos/bytecodes");
  }

  @Transactional
  @DisplayName("메서드에서 사용하는(outgoing), 메서드를 사용하는(ingoing) 메서드들을 연결합니다.")
  @Test
  void createMethodCallGraph() {
    // given
    String cloneUrl = "https://github.com/OpensourceReader/backend.git";
    String reference = "HEAD";
    String savedLocalPath =
        gitRepositoryService.saveToLocal(cloneUrl, bareCloneRepoPath.toString());
    OpenSourceRepo openSourceRepo =
        openSourceRepoService.createRepo(savedLocalPath, cloneUrl, reference);

    // when
    localGitRepoContentMethodCallGraphService.createMethodCallGraph(savedLocalPath, reference);

    // then
    String url = "com/opensourcereader/core/analysis/service/impl/LocalOpenSourceRepoService.java";
    String methodSignature = "createRepoString.String.String";
    Optional<CodeMethodMetaData> codeMethodMetaData =
        codeMethodMetaDataRepository.findByRepoContentPathAndMethodSignature(url, methodSignature);

    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(codeMethodMetaData).isPresent();
          softly
              .assertThat(codeMethodMetaData.get().getOutgoingCalls())
              .extracting(edge -> edge.getCallee().getMethodName())
              .containsExactlyInAnyOrder(
                  "getFlatTree",
                  "createRepositoryBuilder",
                  "getRawText",
                  "addContent",
                  "of",
                  "validateAlreadyExist");
          softly
              .assertThat(codeMethodMetaData.get().getIngoingCalls())
              .extracting(
                  edge -> edge.getCaller().getOpenSourceRepoContent().getPath(),
                  edge -> edge.getCaller().getMethodName())
              .containsExactlyInAnyOrder(
                  Tuple.tuple(
                      "core/src/main/java/com/opensourcereader/core/analysis/service/OpenSourceRepoService.java",
                      "createRepo"));
        });
  }
}
