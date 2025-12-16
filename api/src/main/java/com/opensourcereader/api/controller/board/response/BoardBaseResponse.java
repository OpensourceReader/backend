package com.opensourcereader.api.controller.board.response;

import com.opensourcereader.core.user.dto.UserDto;

import lombok.Data;

@Data
public class BoardBaseResponse {
  private Long id;
  private Long tagId;
  private String title;
  private UserDto boardAuthor;
}
