package com.opensourcereader.api.controller.board.response;

import java.util.List;

import com.opensourcereader.core.board.dto.PullCommentDto;
import com.opensourcereader.core.board.dto.ReviewDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BoardPullResponse extends BoardBaseResponse {

  private List<ReviewDto> reviews;
  private List<PullCommentDto> comment;
}
