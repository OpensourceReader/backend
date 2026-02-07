package com.opensourcereader.core.collaboration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.collaboration.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findAllByPullId(Long pullId);

  Optional<Review> findByProviderId(Long providerId);
}
