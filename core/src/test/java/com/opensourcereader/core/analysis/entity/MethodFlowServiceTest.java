package com.opensourcereader.core.analysis.entity;

import static com.opensourcereader.core.analysis.OpenSourceRepoServiceTestFixture.OPEN_SOURCE_REPO_SERVICE_JAVA_SOURCE;

import java.util.List;

import com.opensourcereader.core.analysis.service.impl.MethodFlowService;
import com.opensourcereader.core.analysis.service.impl.MethodFlowService.PathAndMethod;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MethodFlowServiceTest {
  MethodFlowService methodFlowService = new MethodFlowService();

  @DisplayName("현재 메서드가 사용하는 함수들을 보여줍니다.")
  @Test
  void flowTest() {
    // given & when
    String methodName = "createRepo";
    List<PathAndMethod> pathAndMethods =
        methodFlowService.extractPathAndMethod(OPEN_SOURCE_REPO_SERVICE_JAVA_SOURCE, methodName);

    // then
    Assertions.assertThat(pathAndMethods)
        .extracting(PathAndMethod::path, PathAndMethod::methodName)
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
  }
}
