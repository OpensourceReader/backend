package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;

public interface CodeMethodCallEdgeRepository extends JpaRepository<CodeMethodCallEdge, Long> {

  // 테스트용
  @Query(
      """
      select edge
      from  CodeMethodCallEdge edge
      where edge.caller.methodName  = :callerName
            and edge.callee.methodName = :calleeName
      """)
  Optional<CodeMethodCallEdge> findCallerNameAndCalleeName(
      @Param("callerName") String callerName, @Param("calleeName") String calleeName);
}
