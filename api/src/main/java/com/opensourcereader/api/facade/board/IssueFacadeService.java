package com.opensourcereader.api.facade.board;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.controller.board.request.IssueGetRequest;
import com.opensourcereader.api.controller.board.response.IssueResponse;
import com.opensourcereader.core.board.dto.IssueCommentDto;
import com.opensourcereader.core.board.dto.IssueDto;
import com.opensourcereader.core.board.dto.LabelDto;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.IssueComment;
import com.opensourcereader.core.board.entity.Label;
import com.opensourcereader.core.board.service.IssueCommentService;
import com.opensourcereader.core.board.service.IssueService;
import com.opensourcereader.core.board.service.LabelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacadeService {

  private final IssueService issueService;
  private final LabelService labelService;
  private final IssueCommentService issueCommentService;

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

      List<IssueComment> issueComments = issueCommentService.findAllByIssueId(issueEntity.getId());
      List<IssueCommentDto> comments =
          issueComments.stream()
              .map(issueComment -> new IssueCommentDto(issueComment.getBody()))
              .toList();
      IssueResponse response = new IssueResponse(issue, labels, comments);
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

    List<IssueComment> issueComments = issueCommentService.findAllByIssueId(issueEntity.getId());
    List<IssueCommentDto> comments =
        issueComments.stream()
            .map(issueComment -> new IssueCommentDto(issueComment.getBody()))
            .toList();

    return new IssueResponse(issue, labels, comments);
  }
}
