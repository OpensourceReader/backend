package com.opensourcereader.api.facade.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.api.IntegrationTestSupport;
import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OpenSourceRepoFacadeTest extends IntegrationTestSupport {

  @Autowired
  OpenSourceRepoFacade opensourceRepoFacade;

  @Disabled
  @Transactional
  @DisplayName("오픈소스레포 객체를 생성합니다.")
  @Test
  void createRepo() {
    // given
    String openSourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String reference = "HEAD";
    OpenSourceRepoCreateRequest request =
        new OpenSourceRepoCreateRequest(openSourceUri, reference);

    // when
    OpenSourceRepoResponse response = opensourceRepoFacade.createRepo(request);

    // then
    assertThat(response).isNotNull();
  }
}
