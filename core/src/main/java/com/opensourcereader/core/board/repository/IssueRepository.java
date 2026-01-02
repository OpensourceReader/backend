package com.opensourcereader.core.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.board.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  Optional<Issue> findByRepositoryIdAndTagId(Long repositoryId, Integer tagId);

  List<Issue> findAllByRepositoryIdAndIsOpenedOrderByCreatedAtDesc(
      Long repositoryId, Boolean isOpened);

  Optional<Issue> findByRepositoryOwnerLoginNameAndRepositoryTitleAndTagId(
      String repositoryOwnerLoginName, String repositoryTitle, Integer tagId);

  @Query(
      """
    SELECT i FROM Issue i
      JOIN i.repository r
      JOIN r.owner o
      WHERE o.loginName = :owner
      AND r.title = :repoName
      AND i.commentCount >= :minCommentCount""")
  List<Issue> findAllIssuesByRepo(
      @Param("owner") String owner,
      @Param("repoName") String repoName,
      @Param("minCommentCount") int minCommentCount);
}
