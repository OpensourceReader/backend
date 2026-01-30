package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.Method;

public interface MethodRepository extends JpaRepository<Method, Long> {

  @Query(
      """
          SELECT m
          FROM Method m
          WHERE m.declaredType.openSourceRepoContent.openSourceRepo.id = :repoId
              AND m.declaredType.typeInternalName = :typeInternalName
              AND m.methodSignature.methodSignature = :methodSignature

          """)
  Optional<Method> findCodeMethod(
      @Param("repoId") Long repoId,
      @Param("typeInternalName") String typeInternalName,
      @Param("methodSignature") String methodSignature);

  @Query(
      """
        SELECT DISTINCT m
        FROM Method m
        LEFT JOIN FETCH m.declaredType dt
        LEFT JOIN FETCH dt.openSourceRepoContent orc
        LEFT JOIN FETCH m.outgoingCalls oc
        LEFT JOIN FETCH oc.callee
        WHERE m.id = :codeMethodId
      """)
  Optional<Method> findWithOutgoingGraphById(Long codeMethodId);

  @Query(
      """
      SELECT DISTINCT m
      FROM Method m
      LEFT JOIN FETCH m.declaredType dt
      LEFT JOIN FETCH dt.openSourceRepoContent orc
      LEFT JOIN FETCH m.ingoingCalls ic
      LEFT JOIN FETCH ic.caller
      WHERE m.id = :codeMethodId
      """)
  Optional<Method> findWithIngoingGraphById(Long codeMethodId);
}
