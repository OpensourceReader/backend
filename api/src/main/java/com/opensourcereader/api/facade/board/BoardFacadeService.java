package com.opensourcereader.api.facade.board;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.controller.board.request.BoardGetRequest;
import com.opensourcereader.api.controller.board.response.BoardBaseResponse;
import com.opensourcereader.api.controller.board.response.BoardPreviewResponse;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.service.IssueRetrieveService;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardFacadeService {

  private final IssueRetrieveService issueRetrieveService;

  public List<BoardPreviewResponse> findAllPreviewByRepositoryId(BoardGetRequest request) {
    List<BoardPreviewResponse> responses = new ArrayList<>();
    Long repositoryId = request.repositoryId();

    List<Issue> issueEntities = issueRetrieveService.findIssuesByRepositoryId(repositoryId, true);

    for (Issue entity : issueEntities) {
      UserDto userDto = UserDto.from(entity.getAuthor());
      BoardPreviewResponse response;
      if (entity instanceof Pull pull) {
        response = BoardPreviewResponse.of(pull, userDto);
      } else {
        response = BoardPreviewResponse.of(entity, userDto);
      }
      responses.add(response);
    }

    return responses;
  }

  public BoardBaseResponse findByTagId(Integer tagId, BoardGetRequest request) {
    Long repositoryId = request.repositoryId();

    Issue entity = issueRetrieveService.findIssueOrPullByTagId(repositoryId, tagId);
    if (entity instanceof Pull pull) {
      UserDto user = UserDto.from(pull.getAuthor());

      return BoardBaseResponse.ofPull(pull, user);
    } else {
      UserDto user = UserDto.from(entity.getAuthor());

      return BoardBaseResponse.ofIssue(entity, user);
    }
  }
}
