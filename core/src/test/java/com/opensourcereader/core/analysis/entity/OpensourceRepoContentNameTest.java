package com.opensourcereader.core.analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpensourceRepoContentNameTest {

  @DisplayName("Path로 부터 이름을 추출합니다.")
  @Test
  void extractNameFromPath() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpensourceRepoContentName opensourceRepoContentName = OpensourceRepoContentName.from(path);

    // then
    assertThat(opensourceRepoContentName.getName()).isEqualTo("ci-report.yml");
  }

}