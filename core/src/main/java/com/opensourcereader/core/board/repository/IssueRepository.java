package com.opensourcereader.core.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
    INSERT INTO issues(
      id,
      tag_id,
      title,
      body,
      is_opened,
      comment_count,
      repository_id,
      author_id,
      created_at,
      updated_at,
      disabled
    ) VALUES(
      :#{#issue.id},
      :#{#issue.tagId},
      :#{#issue.title},
      :#{#issue.body},
      :#{#issue.isOpened},
      :#{#issue.commentCount},
      :#{#issue.repository.id},
      :#{#issue.author.id},
      :#{#issue.createdAt},
      :#{#issue.updatedAt},
      :#{#issue.disabled}
    )
    ON DUPLICATE KEY UPDATE
      title = VALUES(:#{#issue.title}),
      body = VALUES(:#{#issue.body}),
      is_opened = VALUES(:#{#issue.isOpened}),
      comment_count = VALUES(:#{#issue.commentCount}),
      updated_at = VALUES(:#{#issue.updatedAt})
""",
      nativeQuery = true)
  void upsert(@Param("issue") Issue issue);

  Optional<Issue> findByRepositoryIdAndTagId(Long repositoryId, Long tagId);

  List<Issue> findAllByRepositoryIdAndIsOpened(Long repositoryId, Boolean isOpened);

  boolean existsByRepositoryIdAndTagId(Long repositoryId, Long tagId);
}
