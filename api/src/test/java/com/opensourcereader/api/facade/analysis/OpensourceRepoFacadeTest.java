package com.opensourcereader.api.facade.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.api.IntegrationTestSupport;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OpensourceRepoFacadeTest extends IntegrationTestSupport {

  @Autowired
  OpensourceRepoFacade opensourceRepoFacade;

  @Transactional
  @DisplayName("오픈소스레포 객체를 생성합니다.")
  @Test
  void create() {
    // given
    String opensourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String localPath = "./local-repos/hibernate-orm.git";
    String reference = "HEAD";

    // when
    OpensourceRepo opensourceRepo = opensourceRepoFacade.create(opensourceUri, localPath,
        reference);

    // then
    assertThat(opensourceRepo).isNotNull();
  }

}