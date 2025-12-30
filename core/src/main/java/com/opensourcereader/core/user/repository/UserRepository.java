package com.opensourcereader.core.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opensourcereader.core.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findFirstByEmail(String email);

  Optional<User> findFirstByLoginName(String loginName);

  Optional<User> findByProviderId(Long providerId);

  Optional<User> findUserByLoginNameAndEmail(String loginName, String email);

  boolean existsByLoginNameAndEmail(String loginName, String email);
}
