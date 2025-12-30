package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;

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
}
