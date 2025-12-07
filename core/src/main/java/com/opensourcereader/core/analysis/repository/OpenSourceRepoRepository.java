package com.opensourcereader.core.analysis.repository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenSourceRepoRepository extends JpaRepository<OpenSourceRepo, Long> {

}
