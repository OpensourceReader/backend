package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;

public interface CodeMethodMetaDataRepository extends JpaRepository<CodeMethodMetaData, Long> {

  @Query(
      """
      SELECT meta
      FROM CodeMethodMetaData meta
      WHERE meta.openSourceRepoContent.path LIKE %:path
          AND meta.methodName = :methodName
      """)
  Optional<CodeMethodMetaData> findByRepoPathAndMethodName(
      @Param("path") String path, @Param("methodName") String methodName);
}
