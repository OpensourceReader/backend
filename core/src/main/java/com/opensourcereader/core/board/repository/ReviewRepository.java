package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
              INSERT INTO reviews(
                id,
                created_at,
                updated_at,
                author_id,
                pull_id,
                body,
                disabled
              ) VALUES(
              :#{#review.id},
              :#{#review.createdAt},
              :#{#review.updatedAt},
              :#{#review.author.id},
              :#{#review.pull.id},
              :#{#review.body},
              :#{#review.disabled}
              )
              ON DUPLICATE KEY UPDATE
                        body = :#{#review.body},
                        updated_at = :#{#review.updatedAt}
              """,
      nativeQuery = true)
  void upsert(@Param("review") Review review);

  List<Review> findAllByPullId(Long pullId);
}
