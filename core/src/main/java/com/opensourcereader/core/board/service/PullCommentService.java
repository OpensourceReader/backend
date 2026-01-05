package com.opensourcereader.core.board.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.dto.PullCommentCommand;
import com.opensourcereader.core.board.entity.PullComment;
import com.opensourcereader.core.board.repository.PullCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PullCommentService {

  private final PullCommentRepository pullCommentRepository;

  @Transactional
  public boolean savePullComments(List<PullCommentCommand> commands) {
    List<PullComment> entities = new ArrayList<>();
    for (PullCommentCommand command : commands) {
      PullComment pullComment = PullComment.from(command);
      entities.add(pullComment);
    }
    pullCommentRepository.saveAll(entities);
    return true;
  }

  @Transactional(readOnly = true)
  public List<PullComment> findAllByReviewId(Long reviewId) {
    return pullCommentRepository.findAllByReviewId(reviewId);
  }
}
