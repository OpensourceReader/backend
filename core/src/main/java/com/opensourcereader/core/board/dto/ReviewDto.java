package com.opensourcereader.core.board.dto;

import java.time.Instant;

import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record ReviewDto(
    Long reviewId, Instant createdAt, Instant updatedAt, UserDto author, String body) {

  public static ReviewDto of(UserDto author, Review review) {

    return ReviewDto.builder()
        .reviewId(review.getId())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .author(author)
        .body(review.getBody())
        .build();
  }
}
