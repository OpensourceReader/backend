package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.PullComment;

public interface PullCommentRepository extends JpaRepository<PullComment, Long> {

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
      INSERT INTO PullComments(
        id,
        created_at,
        updated_at,
        author_id,
        review_id,
        body,
       diff_hunk,
         path,
        disabled
      ) VALUES(
      :#{#pullComment.id},
      :#{#pullComment.createdAt},
      :#{#pullComment.updatedAt},
      :#{#pullComment.author.id},
      :#{#pullComment.review.id},
      :#{#pullComment.body},
      :#{#pullComment.diffHunk},
      :#{#pullComment.path},
      :#{#pullComment.disabled}
      )
      ON DUPLICATE KEY UPDATE
                body = VALUES(:#{#pullComment.body}),
                diff_hunk = VALUES(:#{#pullComment.diffHunk}),
                path = VALUES(:#{#pullComment.path}),
                updated_at = VALUES(:#{#pullComment.updatedAt})
      """,
      nativeQuery = true)
  void upsert(@Param("pullComment") PullComment pullComment);

  List<PullComment> findAllByReviewId(Long reviewId);
}
