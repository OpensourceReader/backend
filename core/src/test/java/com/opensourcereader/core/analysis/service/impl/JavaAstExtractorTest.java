package com.opensourcereader.core.analysis.service.impl;

import static com.opensourcereader.core.analysis.OpenSourceRepoServiceTestFixture.OPEN_SOURCE_REPO_CONTEXT;
import static com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.PATH_SEPARATOR;

import java.util.List;

import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.FieldInfo;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.PathAndMethod;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.PathAndType;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.ReceiverMethodName;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JavaAstExtractorTest {

  private final JavaAstExtractor javaAstExtractor = new JavaAstExtractor();

  @DisplayName("현재 메서드가 사용하는 함수들을 보여줍니다.")
  @Test
  void flowTest() {
    // given & when
    String methodName = "createRepo";
    List<PathAndMethod> pathAndMethods =
        javaAstExtractor.extractOutgoingPathAndMethod(OPEN_SOURCE_REPO_CONTEXT, methodName);

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

  @DisplayName("필드에 선언된 타입과 선언명을 추출합니다")
  @Test
  void extractField() {
    // given & when
    List<FieldInfo> fieldInfos = javaAstExtractor.extractField(OPEN_SOURCE_REPO_CONTEXT);

    // then
    Assertions.assertThat(fieldInfos)
        .extracting(FieldInfo::type, FieldInfo::fieldName)
        .containsExactlyInAnyOrder(
            Tuple.tuple("GitRepositoryService", "gitRepositoryService"),
            Tuple.tuple("OpenSourceRepoRepository", "opensourceRepoRepository"));
  }

  @DisplayName("import문을 통해서 Path를 추출합니다.")
  @Test
  void extractPath() {
    // given &  when
    List<PathAndType> pathAndTypes =
        javaAstExtractor.extractPath(OPEN_SOURCE_REPO_CONTEXT, PATH_SEPARATOR);

    // then
    Assertions.assertThat(pathAndTypes)
        .extracting(PathAndType::path, PathAndType::type)
        .containsExactlyInAnyOrder(
            Tuple.tuple("java.util.List", "List"),
            Tuple.tuple("org.springframework.stereotype.Service", "Service"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.dto.GitTreeFileInfo", "GitTreeFileInfo"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.entity.OpenSourceRepo", "OpenSourceRepo"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.entity.OpenSourceRepoContent",
                "OpenSourceRepoContent"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException",
                "OpenSourceRepoAlreadyExistException"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException",
                "OpenSourceRepoNotFoundException"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository",
                "OpenSourceRepoRepository"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.service.GitRepositoryService",
                "GitRepositoryService"),
            Tuple.tuple(
                "com.opensourcereader.core.analysis.service.OpenSourceRepoService",
                "OpenSourceRepoService"),
            Tuple.tuple("jakarta.transaction.Transactional", "Transactional"),
            Tuple.tuple("org.eclipse.jgit.lib.Repository", "Repository"),
            Tuple.tuple("lombok.RequiredArgsConstructor", "RequiredArgsConstructor"));
  }

  @DisplayName("메서드내에서 다른 메서드 호출을 탐색합니다.")
  @Test
  void extractMethodCallTest() {
    // given & when
    String methodName = "createRepo";
    List<ReceiverMethodName> receiverMethodNames =
        javaAstExtractor.extractMethodCall(OPEN_SOURCE_REPO_CONTEXT, methodName);

    // then
    Assertions.assertThat(receiverMethodNames)
        .extracting(ReceiverMethodName::receiver, ReceiverMethodName::methodName)
        .containsExactlyInAnyOrder(
            Tuple.tuple(null, "validateAlreadyExist"),
            Tuple.tuple("gitRepositoryService", "getFlatTree"),
            Tuple.tuple("gitRepositoryService", "createRepositoryBuilder"),
            Tuple.tuple("gitRepositoryService", "getRawText"),
            Tuple.tuple("fileInfo", "blobId"),
            Tuple.tuple("opensourceRepo", "addContent"),
            Tuple.tuple("OpenSourceRepoContent", "of"),
            Tuple.tuple("opensourceRepoRepository", "save"));
  }
}
