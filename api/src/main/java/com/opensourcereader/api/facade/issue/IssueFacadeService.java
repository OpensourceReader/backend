package com.opensourcereader.api.facade.issue;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.controller.issue.request.IssueGetRequest;
import com.opensourcereader.api.controller.issue.response.IssueResponse;
import com.opensourcereader.core.issue.dto.IssueDto;
import com.opensourcereader.core.issue.dto.LabelDto;
import com.opensourcereader.core.issue.entity.Issue;
import com.opensourcereader.core.issue.entity.Label;
import com.opensourcereader.core.issue.service.IssueService;
import com.opensourcereader.core.issue.service.LabelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacadeService {

  private final IssueService issueService;
  private final LabelService labelService;

  public List<IssueResponse> findIssues(IssueGetRequest request) {
    List<Issue> issueEntities = issueService.findAllByRepositoryId(request.repositoryId(), true);

    List<IssueResponse> responses = new ArrayList<>();
    for (Issue issueEntity : issueEntities) {
      List<Label> labelEntities = labelService.findAllByIssue(issueEntity);

      IssueDto issue =
          new IssueDto(
              issueEntity.getTagId(),
              issueEntity.getIsOpened(),
              issueEntity.getTitle(),
              issueEntity.getBody());

      List<LabelDto> labels =
          labelEntities.stream()
              .map(
                  label ->
                      new LabelDto(label.getName(), label.getDescription(), label.getColorCode()))
              .toList();

      IssueResponse response = new IssueResponse(issue, labels);
      responses.add(response);
    }

    return responses;
  }

  public IssueResponse findIssueByTagId(Long tagId, IssueGetRequest request) {
    Issue issueEntity = issueService.findByTagId(request.repositoryId(), tagId);

    List<Label> labelEntities = labelService.findAllByIssue(issueEntity);

    IssueDto issue =
        new IssueDto(
            tagId, issueEntity.getIsOpened(), issueEntity.getTitle(), issueEntity.getBody());
    List<LabelDto> labels =
        labelEntities.stream()
            .map(
                label ->
                    new LabelDto(label.getName(), label.getDescription(), label.getColorCode()))
            .toList();

    return new IssueResponse(issue, labels);
  }
}
