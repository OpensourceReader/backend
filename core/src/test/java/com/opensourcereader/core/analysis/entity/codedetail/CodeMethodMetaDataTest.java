package com.opensourcereader.core.analysis.entity.codedetail;

import static com.opensourcereader.core.analysis.OpenSourceRepoServiceTestFixture.OPEN_SOURCE_REPO_CONTEXT;

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

  @DisplayName("rawData가 .java 클래스이면 메소드 메타데이터들을 추출합니다.")
  @Test
  void createMethodMetaData() {
    // given
    CompilationUnit compilationUnit = StaticJavaParser.parse(OPEN_SOURCE_REPO_CONTEXT);
    MethodDeclaration methodDeclaration = compilationUnit.findAll(MethodDeclaration.class).get(0);
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo("", ContentType.FILE, null), "", new OpenSourceRepo(""));

    // when
    CodeMethodMetaData methodMetaData = CodeMethodMetaData.of(methodDeclaration, content);

    // then
    Assertions.assertThat(methodMetaData)
        .extracting(
            CodeMethodMetaData::getMethodName,
            CodeMethodMetaData::getMethodSignature,
            CodeMethodMetaData::getMethodModifier,
            CodeMethodMetaData::getStartLine,
            CodeMethodMetaData::getEndLine)
        .containsExactly(
            "createRepo",
            "createRepoString.String.String",
            MethodModifier.PUBLIC,
            27, // @Transactional 같은 어노테이션들도 다 포함함
            43);
  }
}
