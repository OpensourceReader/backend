package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;

public interface CodeMethodRepository extends JpaRepository<CodeMethod, Long> {

  @Query(
      """
      SELECT meta
      FROM CodeMethod meta
      WHERE meta.openSourceRepoContent.path LIKE %:path
          AND meta.methodSignature.methodSignature = :methodSignature
      """)
  Optional<CodeMethod> findByRepoContentPathAndMethodSignature(
      @Param("path") String path, @Param("methodSignature") String methodSignature);
}
