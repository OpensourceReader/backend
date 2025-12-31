package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.method.CodeMethod;

public interface CodeMethodRepository extends JpaRepository<CodeMethod, Long> {

  @Query(
      """
          SELECT m
          FROM CodeMethod m
          WHERE m.openSourceRepoContent.openSourceRepo.id = :repoId
              AND m.openSourceRepoContent.classInternalName = :classInternalName
              AND m.methodSignature.methodSignature = :methodSignature

          """)
  Optional<CodeMethod> findByRepoIdAndClassInternalNameAndMethodSignature(
      @Param("repoId") Long repoId,
      @Param("classInternalName") String classInternalName,
      @Param("methodSignature") String methodSignature);

  @Query(
      """
       SELECT DISTINCT m
        FROM CodeMethod m
        LEFT JOIN FETCH m.openSourceRepoContent c
        LEFT JOIN FETCH m.outgoingCalls oc
        LEFT JOIN FETCH oc.callee
        WHERE m.id = :codeMethodId
       """)
  Optional<CodeMethod> findWithOutgoingGraphById(Long codeMethodId);

  @Query(
      """
       SELECT DISTINCT m
        FROM CodeMethod m
        LEFT JOIN FETCH m.openSourceRepoContent c
        LEFT JOIN FETCH m.ingoingCalls ic
        LEFT JOIN FETCH ic.caller
        WHERE m.id = :codeMethodId
       """)
  Optional<CodeMethod> findWithIngoingGraphById(Long codeMethodId);
}
