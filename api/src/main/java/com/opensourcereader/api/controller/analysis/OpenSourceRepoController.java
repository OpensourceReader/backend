package com.opensourcereader.api.controller.analysis;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoCreateResponse;
import com.opensourcereader.api.facade.analysis.OpenSourceRepoFacade;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/opensource-repo")
@RestController
@RequiredArgsConstructor
public class OpenSourceRepoController {

  private final OpenSourceRepoFacade opensourceRepoFacade;

  @PostMapping
  public OpenSourceRepoCreateResponse createRepo(
      @RequestBody OpenSourceRepoCreateRequest repoCreateRequest
  ) {
    return opensourceRepoFacade.create(repoCreateRequest);
  }
}
