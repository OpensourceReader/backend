package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.PullComment;

public interface PullCommentRepository extends JpaRepository<PullComment, Long> {

  List<PullComment> findAllByReviewId(Long reviewId);
}
