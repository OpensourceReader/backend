package com.opensourcereader.core.analysis.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepoIdentifierTest {

  @Test
  @DisplayName("HTTPS clone URL에서 ownerName과 repoName을 추출한다")
  void should_extract_from_https_url() {
    // given
    String url = "https://github.com/OpensourceReader/backend.git";

    // when
    RepoIdentifier repo = RepoIdentifier.of(url);

    // then
    assertThat(repo.ownerName()).isEqualTo("OpensourceReader");
    assertThat(repo.repoName()).isEqualTo("backend");
  }

  @Test
  @DisplayName("SSH clone URL에서 ownerName과 repoName을 추출한다")
  void should_extract_from_ssh_url() {
    // given
    String url = "git@github.com:OpensourceReader/backend.git";

    // when
    RepoIdentifier repo = RepoIdentifier.of(url);

    // then
    assertThat(repo.ownerName()).isEqualTo("OpensourceReader");
    assertThat(repo.repoName()).isEqualTo("backend");
  }

  @Test
  @DisplayName(".git이 없는 경우도 정상 처리한다")
  void should_handle_without_git_suffix() {
    // given
    String url = "https://github.com/OpensourceReader/backend";

    // when
    RepoIdentifier repo = RepoIdentifier.of(url);

    // then
    assertThat(repo.ownerName()).isEqualTo("OpensourceReader");
    assertThat(repo.repoName()).isEqualTo("backend");
  }

  @Test
  @DisplayName("잘못된 URL이면 예외를 던진다")
  void should_throw_exception_when_invalid_url() {
    // given
    String url = "invalid-url";

    // when & then
    assertThatThrownBy(() -> RepoIdentifier.of(url))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid repository URL");
  }
}
