package com.opensourcereader.core.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  Optional<Issue> findByRepositoryIdAndTagId(Long repositoryId, Long tagId);

  List<Issue> findAllByRepositoryIdAndIsOpened(Long repositoryId, Boolean isOpened);

  boolean existsByRepositoryIdAndTagId(Long repositoryId, Long tagId);
}
