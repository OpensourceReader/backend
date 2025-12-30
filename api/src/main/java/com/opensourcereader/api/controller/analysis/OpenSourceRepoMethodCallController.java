package com.opensourcereader.api.controller.analysis;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.dto.CodeMethodResponse;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.service.CodeMethodGraphQueryService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/opensource-repo/methods")
@RestController
@RequiredArgsConstructor
public class OpenSourceRepoMethodCallController {

  private final CodeMethodGraphQueryService codeMethodGraphQueryService;

  @GetMapping("/{codeMethodId}")
  public ResponseEntity<CodeMethodResponse> getMethodGraphView(@PathVariable Long codeMethodId) {
    CodeMethod codeMethodById = codeMethodGraphQueryService.getCodeMethodById(codeMethodId);
    return ResponseEntity.ok(CodeMethodResponse.of(codeMethodById));
  }
}
