package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.domain.entity.Type;

public interface TypeRepository extends JpaRepository<Type, Long> {

  @Query(
      """
            select dt
            from Type dt
            left join dt.openSourceRepoFile orc
            left join orc.openSourceRepo osr
            where osr.id = :repoId
              and dt.typeInternalName = :typeInternalName
          """)
  Optional<Type> findByRepoAndTypeInternalName(
      @Param("repoId") Long repoId, @Param("typeInternalName") String typeInternalName);
}
