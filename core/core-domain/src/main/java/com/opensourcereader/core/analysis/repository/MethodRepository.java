package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.opensourcereader.core.analysis.domain.entity.Method;

public interface MethodRepository extends JpaRepository<Method, Long> {

  @Query(
      """
        SELECT DISTINCT m
        FROM Method m
        LEFT JOIN FETCH m.type dt
        LEFT JOIN FETCH dt.openSourceRepoFile orc
        LEFT JOIN FETCH m.outgoingCalls oc
        LEFT JOIN FETCH oc.callee
        WHERE m.id = :codeMethodId
      """)
  Optional<Method> findWithOutgoingGraphById(Long codeMethodId);

  @Query(
      """
      SELECT DISTINCT m
      FROM Method m
      LEFT JOIN FETCH m.type dt
      LEFT JOIN FETCH dt.openSourceRepoFile orc
      LEFT JOIN FETCH m.ingoingCalls ic
      LEFT JOIN FETCH ic.caller
      WHERE m.id = :codeMethodId
      """)
  Optional<Method> findWithIngoingGraphById(Long codeMethodId);
}
