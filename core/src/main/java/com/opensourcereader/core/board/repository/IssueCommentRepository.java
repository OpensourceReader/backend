package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.IssueComment;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

  @Modifying(clearAutomatically = true)
  @Query(
      value =
          """
              INSERT INTO IssueComments(
                id,
                created_at,
                updated_at,
                author_id,
                issue_id,
                body,
                disabled
              ) VALUES(
              :#{#issueComment.id},
              :#{#issueComment.createdAt},
              :#{#issueComment.updatedAt},
              :#{#issueComment.author.id},
              :#{#issueComment.issue.id},
              :#{#issueComment.body},
              :#{#issueComment.disabled}
              )
              ON DUPLICATE KEY UPDATE
                        body = :#{#issueComment.body},
                        updated_at = :#{#issueComment.updatedAt}
              """,
      nativeQuery = true)
  void upsert(@Param("issueComment") IssueComment issueComment);

  List<IssueComment> findAllByIssueId(Long issueId);
}
