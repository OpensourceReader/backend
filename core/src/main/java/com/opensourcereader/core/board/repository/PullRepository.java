package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.Pull;

public interface PullRepository extends JpaRepository<Pull, Long> {

  List<Pull> findAllByRepositoryIdAndIsOpened(Long repositoryId, Boolean isOpened);
}
