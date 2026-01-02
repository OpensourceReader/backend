package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.entity.Pull;

public interface PullRepository extends JpaRepository<Pull, Long> {

  @Query("""
   SELECT p.tagId FROM Pull p
    JOIN p.repository r
    WHERE r = :repo
  """)
  List<Integer> findAllByRepository(@Param("repo") OpenSourceRepo repo);
}
