package com.opensourcereader.core.board.repository;

import com.opensourcereader.core.board.entity.PullComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullCommentRepository extends JpaRepository<PullComment, Long> {

  List<PullComment> findAllByReviewId(Long reviewId);
}
