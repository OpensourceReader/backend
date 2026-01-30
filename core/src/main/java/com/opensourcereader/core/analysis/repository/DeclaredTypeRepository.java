package com.opensourcereader.core.analysis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;

public interface DeclaredTypeRepository extends JpaRepository<DeclaredType, Long> {

  @Query(
      """
            select dt
            from DeclaredType dt
            left join dt.openSourceRepoContent orc
            left join orc.openSourceRepo osr
            where osr.id = :repoId
              and dt.typeKind = :typeKind
          """)
  List<DeclaredType> findByRepoAndTypesByKind(
      @Param("repoId") Long repoId, @Param("typeKind") TypeKind typeKind);

  @Query(
      """
            select dt
            from DeclaredType dt
            left join dt.openSourceRepoContent orc
            left join orc.openSourceRepo osr
            where osr.id = :repoId
              and dt.typeInternalName = :internalName
          """)
  Optional<DeclaredType> findByRepoAndTypeInternalName(
      @Param("repoId") Long repoId, @Param("internalName") String internalName);
}
