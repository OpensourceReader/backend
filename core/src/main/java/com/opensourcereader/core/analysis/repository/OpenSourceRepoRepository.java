package com.opensourcereader.core.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;

public interface OpenSourceRepoRepository extends JpaRepository<OpenSourceRepo, Long> {}
