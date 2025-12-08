package com.opensourcereader.core.analysis.service.basic;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.dto.GitTree;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalGitRepositoryServiceTest {

  LocalGitRepositoryService localGitRepositoryService = new LocalGitRepositoryService();

  @Disabled
  @DisplayName("레포를 로컬 볼륨에 저장합니다.")
  @Test
  void saveToLocal() {
    // given
    String openSourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String localDirectory = "test-repos";

    // when
    String savedLocalPath = localGitRepositoryService.saveToLocal(openSourceUri, localDirectory);

    // then
    assertThat(savedLocalPath).isNotNull();
  }

  @Disabled
  @DisplayName("같은 이름의 폴더가 있으면, 다시 저장하지 않고 폴더만 다시 반환합니다.")
  @Test
  void saveToLocalSameName() {
    // given
    String openSourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String localPath = "./local-repos/hibernate-orm.git";

    // when
    String savedLocalPath = localGitRepositoryService.saveToLocal(openSourceUri, localPath);

    // then
    assertThat(savedLocalPath).isNotNull();
  }

  @Disabled
  @DisplayName("레포의 트리구조를 반환합니다.")
  @Test
  void searchTest() {
    // given
    String localPath = "./local-repos/hibernate-orm.git";
    String reference = "HEAD";

    // when
    GitTree treeOfRepo = localGitRepositoryService.getFlatTree(localPath, reference);

    // then
    assertThat(treeOfRepo.fileInfos()).isNotNull();
  }

}