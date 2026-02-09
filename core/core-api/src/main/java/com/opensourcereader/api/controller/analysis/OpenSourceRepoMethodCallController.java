package com.opensourcereader.api.controller.analysis;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.api.dto.CodeMethodResponse;
import com.opensourcereader.api.facade.analysis.OpenSourceCodeMethodFacade;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/opensource-repo/methods")
@RestController
@RequiredArgsConstructor
public class OpenSourceRepoMethodCallController {

  private final OpenSourceCodeMethodFacade codeMethodFacade;

  @GetMapping
  public ResponseEntity<CodeMethodResponse> getMethodGraphView(
      @RequestBody CodeMethodRequest request) {
    return ResponseEntity.ok(codeMethodFacade.getCodeMethodById(request));
  }
}
