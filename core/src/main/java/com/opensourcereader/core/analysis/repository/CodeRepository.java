package com.opensourcereader.core.analysis.repository;

import com.opensourcereader.core.analysis.entity.RepositoryContent;
import org.springframework.data.repository.Repository;

public interface CodeRepository extends Repository<RepositoryContent, Long> {

}
