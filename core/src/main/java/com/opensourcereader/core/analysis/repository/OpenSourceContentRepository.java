package com.opensourcereader.core.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;

public interface OpenSourceContentRepository extends JpaRepository<OpenSourceRepoContent, Long> {}
