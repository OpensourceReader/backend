package com.opensourcereader.core.collaboration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.collaboration.entity.Pull;

public interface PullRepository extends JpaRepository<Pull, Long> {

  @Query("""
   SELECT p.tagId FROM Pull p
    JOIN p.repository r
    WHERE r = :repo
  """)
  List<Integer> findAllByRepository(@Param("repo") OpenSourceRepo repo);
}
