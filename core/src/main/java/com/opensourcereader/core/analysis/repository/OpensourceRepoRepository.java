package com.opensourcereader.core.analysis.repository;

import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpensourceRepoRepository extends JpaRepository<OpensourceRepo, Long> {

}
