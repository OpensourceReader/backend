package com.opensourcereader.core.analysis.service.impl;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SpringBootTest
class MethodFlowServiceTest {

  @Autowired private MethodFlowService methodFlowService;
  @Autowired private OpenSourceRepoService openSourceRepoService;
  @Autowired private GitRepositoryService gitRepositoryService;
  @Autowired private CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @TempDir private Path tempDir;

  @Transactional
  @DisplayName("메서드 흐름 생성")
  @Test
  void createMethodFlow() {
    // given
    String cloneUrl = "https://github.com/OpensourceReader/backend.git";
    Path repoDir = tempDir.resolve("repos");
    String reference = "HEAD";

    String savedPath = gitRepositoryService.saveToLocal(cloneUrl, repoDir.toString());
    OpenSourceRepo repo = openSourceRepoService.createRepo(savedPath, cloneUrl, reference);

    // when
    methodFlowService.createMethodFlow(cloneUrl);

    // then
    String path = "com/opensourcereader/core/analysis/service/impl/LocalOpenSourceRepoService.java";
    String methodName = "createRepo";
    CodeMethodMetaData codeMethodMetaData =
        codeMethodMetaDataRepository
            .findByRepoContentPathAndMethodSignature(path, methodName)
            .get();
    Assertions.assertThat(codeMethodMetaData.getOutgoingCalls())
        .extracting(
            codeEdge -> codeEdge.getCallee().getOpenSourceRepoContent().getPath(),
            codeEdge -> codeEdge.getCallee().getMethodName())
        .containsExactlyInAnyOrder(
            Tuple.tuple(
                "com.opensourcereader.core.analysis.service.GitRepositoryService", "getFlatTree"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.service.GitRepositoryService",
                "createRepositoryBuilder"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.service.GitRepositoryService", "getRawText"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository", "save"));
    Assertions.assertThat(codeMethodMetaData.getIngoingCalls())
        .extracting(
            codeEdge -> codeEdge.getCaller().getOpenSourceRepoContent().getPath(),
            codeEdge -> codeEdge.getCaller().getMethodName())
        .containsExactlyInAnyOrder(
            Tuple.tuple("com.opensourcereader.api.facade.analysis", "createRepo"));
  }
}
