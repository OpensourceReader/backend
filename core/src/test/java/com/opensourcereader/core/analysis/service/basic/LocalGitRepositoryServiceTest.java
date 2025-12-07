package com.opensourcereader.core.analysis.service.basic;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.dto.GitTree;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalGitRepositoryServiceTest {

  LocalGitRepositoryService localGitRepositoryService = new LocalGitRepositoryService();

  @Disabled
  @DisplayName("레포를 로컬 볼륨에 저장합니다.")
  @Test
  void saveLocalToDirectory() {
    // given
    String opensourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String localPath = "./local-repos/hibernate-orm.git";

    // when
    Repository repo = localGitRepositoryService.saveLocalToDirectory(opensourceUri, localPath);

    // then
    assertThat(repo).isNotNull();
  }

  @Disabled
  @DisplayName("같은 이름의 폴더가 있으면, 다시 저장하지 않고 폴더만 다시 반환합니다.")
  @Test
  void saveLocalToDirectorySameName() {
    // given
    String opensourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String localPath = "./local-repos/hibernate-orm.git";

    // when
    Repository repo = localGitRepositoryService.saveLocalToDirectory(opensourceUri, localPath);

    // then
    assertThat(repo).isNotNull();
  }

  @DisplayName("레포의 트리구조를 반환합니다.")
  @Test
  void searchTest() throws IOException {
    // given
    String localPath = "./local-repos/hibernate-orm.git";
    String reference = "HEAD";
    Repository repo = new FileRepositoryBuilder()
        .setGitDir(new File(localPath))
        .build();

    // when
    GitTree treeOfRepo = localGitRepositoryService.getFlatTreeOfRepo(repo, reference);

    // then
    assertThat(treeOfRepo.fileInfos()).isNotNull();
  }

}