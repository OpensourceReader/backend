package com.opensourcereader.core.activity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.activity.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findAllByPullId(Long pullId);

  Optional<Review> findByProviderId(Long providerId);
}
