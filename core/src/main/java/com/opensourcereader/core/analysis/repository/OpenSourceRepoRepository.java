package com.opensourcereader.core.analysis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.user.entity.User;

public interface OpenSourceRepoRepository extends JpaRepository<OpenSourceRepo, Long> {

  boolean existsByCloneUrl(String cloneUrl);

  Optional<OpenSourceRepo> findByOwnerLoginNameAndTitle(String ownerLoginName, String title);

  Optional<OpenSourceRepo> findByOwnerAndTitle(User owner, String title);
}
