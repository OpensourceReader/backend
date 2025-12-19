package com.opensourcereader.core.board.service;

import java.util.List;

import com.opensourcereader.core.board.dto.PullCommentCommand;
import com.opensourcereader.core.board.entity.PullComment;

public interface PullCommentService {

  PullComment create(PullCommentCommand command);

  List<PullComment> findAllByReviewId(Long reviewId);

  void deleteSoftById(Long id);
}
