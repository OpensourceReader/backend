package com.opensourcereader.core.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.security.entity.RefreshToken;
import com.opensourcereader.core.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByUser(User user);
}
