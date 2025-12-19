package com.opensourcereader.core.analysis.entity;

import static com.opensourcereader.core.analysis.OpenSourceRepoServiceTestFixture.HIBERNATE_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.entity.codedetail.MethodModifier;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenSourceRepoContentNameTest {

  @DisplayName("Path로 부터 이름을 추출합니다.")
  @Test
  void extractNameFromPath() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpenSourceRepoContentName opensourceRepoContentName = OpenSourceRepoContentName.from(path);

    // then
    assertThat(opensourceRepoContentName.getName()).isEqualTo("ci-report.yml");
  }

  @DisplayName("코드의 메서드의 메타 데이터들을 생성합니다.")
  @Test
  void createCodeMethodMetaData() {
    // given
    String path = "IngresSqmToSqlAstConverter.java";

    // when
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo(path, ContentType.FILE, null),
            HIBERNATE_CONTEXT,
            new OpenSourceRepo(""));

    // then
    Assertions.assertThat(content.getCodeMethodMetaData())
        .extracting(
            CodeMethodMetaData::getMethodName,
            CodeMethodMetaData::getMethodModifier,
            CodeMethodMetaData::getStartLine,
            CodeMethodMetaData::getEndLine)
        .containsExactlyElementsOf(
            List.of(
                Tuple.tuple("visitQuerySpec", MethodModifier.PUBLIC, 51, 75),
                Tuple.tuple("resolveGroupOrOrderByExpression", MethodModifier.PROTECTED, 77, 85)));
  }

  @DisplayName("자바파일의 메서드 추출시 자바파일이 아니면 추출하지 않습니다.")
  @Test
  void createCodeMethodMetaDataNotJavaFile() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo(path, ContentType.FILE, null), "", new OpenSourceRepo(""));

    // then
    Assertions.assertThat(content.getCodeMethodMetaData()).isEmpty();
  }
}
