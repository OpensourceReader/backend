package com.opensourcereader.core.board.repository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import java.util.List;
import java.util.Optional;

import java.util.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.Pull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PullRepository extends JpaRepository<Pull, Long> {

  @Query("""
   SELECT p.tagId FROM Pull p
    JOIN p.repository r
    WHERE r = :repo
  """)
  Queue<Integer> findAllByRepository(@Param("repo") OpenSourceRepo repo);
}
