package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;

public interface OpenSourceRepoRepository extends JpaRepository<OpenSourceRepo, Long> {

  boolean existsByCloneUrl(String cloneUrl);

  @Query(
      """
          select r
          from OpenSourceRepo r
          where r.repoIdentifier.ownerName = :ownerName
            and r.repoIdentifier.repoName = :repoName
      """)
  Optional<OpenSourceRepo> findByOwnerAndRepo(
      @Param("ownerName") String ownerName, @Param("repoName") String repoName);
}
