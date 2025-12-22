package com.opensourcereader.core.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.Pull;

public interface PullRepository extends JpaRepository<Pull, Long> {

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
                  INSERT INTO pulls(
                    id,
                    tag_id,
                    title,
                    body,
                    is_opened,
                    comment_count,
                    review_count,
                    repository_id,
                    author_id,
                    created_at,
                    updated_at,
                    disabled
                  ) VALUES(
                    :#{#pull.id},
                    :#{#pull.tagId},
                    :#{#pull.title},
                    :#{#pull.body},
                    :#{#pull.isOpened},
                    :#{#pull.commentCount},
                    :#{#pull.reviewCount},
                    :#{#pull.repository.id},
                    :#{#pull.author.id},
                    :#{#pull.createdAt},
                    :#{#pull.updatedAt},
                    :#{#pull.disabled}
                  )
                  ON DUPLICATE KEY UPDATE
                    title = :#{#pull.title},
                    body = :#{#pull.body},
                    is_opened = :#{#pull.isOpened},
                    comment_count = :#{#pull.commentCount},
                    updated_at = :#{#pull.updatedAt}
              """,
      nativeQuery = true)
  void upsert(@Param("pull") Pull pull);

  Optional<Pull> findByRepositoryIdAndTagId(Long repositoryId, Long tagId);

  List<Pull> findAllByRepositoryIdAndIsOpened(Long repositoryId, Boolean isOpened);

  boolean existsByRepositoryIdAndTagId(Long repositoryId, Long tagId);
}
