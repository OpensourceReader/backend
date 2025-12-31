package com.opensourcereader.core.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  Optional<Issue> findByRepositoryIdAndTagId(Long repositoryId, Integer tagId);

  List<Issue> findAllByRepositoryIdAndIsOpenedOrderByCreatedAtDesc(
      Long repositoryId, Boolean isOpened);

  Optional<Issue> findByRepositoryOwnerLoginNameAndRepositoryTitleAndTagId(
      String repositoryOwnerLoginName, String repositoryTitle, Integer tagId);
}
