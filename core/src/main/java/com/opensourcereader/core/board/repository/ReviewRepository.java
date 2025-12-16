package com.opensourcereader.core.board.repository;

import com.opensourcereader.core.board.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {

  List<Review> findAllByPullId(Long pullId);
}
