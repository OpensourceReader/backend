package com.opensourcereader.core.analysis.repository;

import com.opensourcereader.core.analysis.entity.OpensourceRepoContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpensourceRepoContentRepository extends
    JpaRepository<OpensourceRepoContent, Long> {

}
