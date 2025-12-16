package com.opensourcereader.api.controller.board.response;

import java.util.List;

import com.opensourcereader.core.board.dto.IssueCommentDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BoardIssueResponse extends BoardBaseResponse {

  private List<IssueCommentDto> comments;
}
