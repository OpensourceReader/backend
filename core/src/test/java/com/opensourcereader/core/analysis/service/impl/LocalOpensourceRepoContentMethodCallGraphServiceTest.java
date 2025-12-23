package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SpringBootTest
class LocalOpensourceRepoContentMethodCallGraphServiceTest {

  @Autowired private GitRepositoryService gitRepositoryService;
  @Autowired private OpenSourceRepoService openSourceRepoService;

  @Autowired
  private LocalOpensourceRepoContentMethodCallGraphService
      localGitRepoContentMethodCallGraphService;

  @Autowired private CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @TempDir private Path tempDir;

  private Path bareCloneRepoPath;
  private static final String WORKING_TREE_DIR_NAME = "workingTreeDirName";

  @BeforeEach
  void setup() {
    bareCloneRepoPath = tempDir.resolve("repos");
  }

  @Transactional
  @DisplayName("메서드에서 사용하는(outgoing), 메서드를 사용하는(ingoing) 메서드들을 연결합니다.")
  @Test
  void createMethodCallGraph() {
    // given
    String cloneUrl = "https://github.com/OpensourceReader/backend.git";
    String reference = "HEAD";
    Path savedLocalPath = gitRepositoryService.saveToLocal(cloneUrl, bareCloneRepoPath.toString());
    openSourceRepoService.createRepo(savedLocalPath, cloneUrl, reference);

    // when
    localGitRepoContentMethodCallGraphService.createMethodCallGraph(
        savedLocalPath, reference, WORKING_TREE_DIR_NAME);

    // then
    String url = "com/opensourcereader/core/analysis/service/impl/LocalOpenSourceRepoService.java";
    String methodSignature =
        CodeMethodSignature.of("createRepo", List.of("String", "String", "String"))
            .methodSignature();
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
