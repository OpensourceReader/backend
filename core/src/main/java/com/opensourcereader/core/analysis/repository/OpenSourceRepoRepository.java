package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoRepository extends JpaRepository<OpenSourceRepo, Long> {

  boolean existsByCloneUrl(String cloneUrl);

  Optional<OpenSourceRepo> findByCloneUrl(String cloneUrl);
}
