package com.opensourcereader.core.analysis.entity.codedetail;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodeMethodMetaDataTest {

  String sampleText =
      """
      package com.opensourcereader.core.analysis.service.impl;

      import java.util.List;

      import org.springframework.stereotype.Service;

      import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
      import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
      import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
      import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
      import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
      import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
      import com.opensourcereader.core.analysis.service.GitRepositoryService;
      import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
      import jakarta.transaction.Transactional;
      import org.eclipse.jgit.lib.Repository;

      import lombok.RequiredArgsConstructor;

      @Service
      @RequiredArgsConstructor
      public class LocalOpenSourceRepoService implements OpenSourceRepoService {

        private final GitRepositoryService gitRepositoryService;
        private final OpenSourceRepoRepository opensourceRepoRepository;

        @Transactional
        @Override
        public OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference) {
          validateAlreadyExist(cloneUrl);

          OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUrl);
          List<GitTreeFileInfo> flatTree =
              gitRepositoryService.getFlatTree(savedLocalPath, repoReference);

          Repository repo = gitRepositoryService.createRepositoryBuilder(savedLocalPath);
          for (GitTreeFileInfo fileInfo : flatTree) {
            String rawText = gitRepositoryService.getRawText(fileInfo.blobId(), repo);
            opensourceRepo.addContent(OpenSourceRepoContent.of(fileInfo, rawText, opensourceRepo));
          }

          return opensourceRepoRepository.save(opensourceRepo);
        }

        @Override
        public OpenSourceRepo getRepoById(Long repositoryId) {
          return opensourceRepoRepository
              .findById(repositoryId)
              .orElseThrow(OpenSourceRepoNotFoundException::new);
        }

        @Override
        public void deleteRepoById(Long repositoryId) {
          opensourceRepoRepository.deleteById(repositoryId);
        }

        private void validateAlreadyExist(String cloneUrl) {
          if (opensourceRepoRepository.existsByCloneUrl(cloneUrl)) {
            throw new OpenSourceRepoAlreadyExistException();
          }
        }
      }

      """;

  @DisplayName("rawData가 .java 클래스이면 메소드메타데이터들을 추출합니다.")
  @Test
  void createMethodMetaData() {
    // given
    CompilationUnit compilationUnit = StaticJavaParser.parse(sampleText);
    MethodDeclaration methodDeclaration = compilationUnit.findAll(MethodDeclaration.class).get(0);
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo("", ContentType.FILE, null), "", new OpenSourceRepo(""));

    // when
    CodeMethodMetaData methodMetaData = CodeMethodMetaData.createFrom(methodDeclaration, content);

    // then
    Assertions.assertThat(methodMetaData)
        .extracting(
            CodeMethodMetaData::getMethodName,
            CodeMethodMetaData::getMethodModifier,
            CodeMethodMetaData::getStartLine,
            CodeMethodMetaData::getEndLine)
        .containsExactly(
            "createRepo",
            MethodModifier.PUBLIC,
            27, // @Transactional 같은 어노테이션들도 다 포함함
            43);
  }
}
