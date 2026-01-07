package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.analysis.entity.repo.DeclaredType;

public interface DeclaredTypeRepository extends JpaRepository<DeclaredType, Long> {
  Optional<DeclaredType> findByTypeInternalName(String name);
}
