package com.opensourcereader.api.facade.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.api.IntegrationTestSupport;
import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenSourceRepoFacadeTest extends IntegrationTestSupport {

  @Autowired OpenSourceRepoFacade opensourceRepoFacade;

  @Disabled("시간과 용량상의 문제로 disabled 처리했습니다.")
  @Transactional
  @DisplayName("오픈소스레포 객체를 생성합니다.")
  @Test
  void createRepo() {
    // given
    String openSourceUri = "https://github.com/hibernate/hibernate-orm.git";
    String reference = "HEAD";
    OpenSourceRepoCreateRequest request = new OpenSourceRepoCreateRequest(openSourceUri, reference);

    // when
    OpenSourceRepoResponse response = opensourceRepoFacade.createRepo(request);

    // then
    assertThat(response).isNotNull();
  }
}
