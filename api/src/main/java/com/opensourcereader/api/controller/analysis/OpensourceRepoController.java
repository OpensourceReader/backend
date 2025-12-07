package com.opensourcereader.api.controller.analysis;

import com.opensourcereader.api.dto.OpensourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpensourceRepoCreateResponse;
import com.opensourcereader.api.facade.analysis.OpensourceRepoFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/opensource-repo")
@RestController
@RequiredArgsConstructor
public class OpensourceRepoController {

  private final OpensourceRepoFacade opensourceRepoFacade;

  @PostMapping
  public OpensourceRepoCreateResponse createRepo(
      @RequestBody OpensourceRepoCreateRequest repoCreateRequest
  ) {
    return opensourceRepoFacade.create(repoCreateRequest);
  }

}
