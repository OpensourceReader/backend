package com.opensourcereader.core.analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.entity.file.OpenSourceRepoFileName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenSourceRepoFileNameTest {

  @DisplayName("Path로 부터 이름을 추출합니다.")
  @Test
  void extractNameFromPath() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpenSourceRepoFileName opensourceRepoContentName = OpenSourceRepoFileName.from(path);

    // then
    assertThat(opensourceRepoContentName.name()).isEqualTo("ci-report.yml");
  }
}
