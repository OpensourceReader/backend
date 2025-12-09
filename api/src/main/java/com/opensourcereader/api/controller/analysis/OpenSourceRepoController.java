package com.opensourcereader.api.controller.analysis;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.api.facade.analysis.OpenSourceRepoFacade;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/opensource-repo")
@RestController
@RequiredArgsConstructor
public class OpenSourceRepoController {

  private final OpenSourceRepoFacade opensourceRepoFacade;

  @PostMapping
  public OpenSourceRepoResponse createRepo(
      @RequestBody OpenSourceRepoCreateRequest repoCreateRequest) {
    return opensourceRepoFacade.createRepo(repoCreateRequest);
  }

  @GetMapping("/{repoId}")
  public OpenSourceRepoResponse getRepoById(@PathVariable(value = "repoId") Long repoId) {
    return opensourceRepoFacade.getRepoById(repoId);
  }

  @DeleteMapping("/{repoId}")
  public void deleteRepoById(@PathVariable(value = "repoId") Long repoId) {
    opensourceRepoFacade.deleteRepoById(repoId);
  }
}
