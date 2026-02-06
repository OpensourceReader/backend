package com.opensourcereader.core.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.activity.entity.Pull;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;

public interface PullRepository extends JpaRepository<Pull, Long> {

  @Query("""
   SELECT p.tagId FROM Pull p
    JOIN p.repository r
    WHERE r = :repo
  """)
  List<Integer> findAllByRepository(@Param("repo") OpenSourceRepo repo);
}
