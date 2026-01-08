package com.opensourcereader.core.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.PullRequestFile;

public interface PullRequestFileRepository extends JpaRepository<PullRequestFile, Long> {}
