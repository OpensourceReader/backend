package com.opensourcereader.core.activity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.activity.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  Optional<Issue> findByRepositoryIdAndTagId(Long repositoryId, Integer tagId);

  List<Issue> findAllByRepositoryIdAndIsOpenedOrderByCreatedAtDesc(
      Long repositoryId, Boolean isOpened);

  @Query(
      """
    select i
    from Issue i
    where i.repository.repoIdentifier.ownerName = :ownerName
      and i.repository.repoIdentifier.repoName = :repoName
      and i.tagId = :tagId
""")
  Optional<Issue> findIssue(
      @Param("ownerName") String ownerName,
      @Param("repoName") String repoName,
      @Param("tagId") Integer tagId);

  @Query(
      """
    SELECT i
    FROM Issue i
    JOIN i.repository r
    WHERE r.repoIdentifier.ownerName = :owner
      AND r.repoIdentifier.repoName = :repoName
      AND i.commentCount >= :minCommentCount
""")
  List<Issue> findAllIssuesByRepo(
      @Param("owner") String owner,
      @Param("repoName") String repoName,
      @Param("minCommentCount") int minCommentCount);
}
