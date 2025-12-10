package com.opensourcereader.api.controller.issue;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.controller.issue.request.IssueGetRequest;
import com.opensourcereader.api.controller.issue.response.IssueResponse;
import com.opensourcereader.api.facade.issue.IssueFacadeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueController {

  private final IssueFacadeService issueFacadeService;

  @GetMapping
  public ResponseEntity<List<IssueResponse>> findIssues(@RequestBody IssueGetRequest request) {
    List<IssueResponse> response = issueFacadeService.findIssues(request);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{tagId}")
  public ResponseEntity<IssueResponse> findIssueByTagId(
      @PathVariable Long tagId, @RequestBody IssueGetRequest request) {
    IssueResponse response = issueFacadeService.findIssueByTagId(tagId, request);

    return ResponseEntity.ok(response);
  }
}
