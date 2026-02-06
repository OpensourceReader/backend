package com.opensourcereader.core.activity.dto;

import java.time.Instant;

import com.opensourcereader.core.activity.entity.Review;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record ReviewDto(Long reviewId, Instant submittedAt, UserDto author, String body) {

  public static ReviewDto of(UserDto author, Review review) {

    return ReviewDto.builder()
        .reviewId(review.getId())
        .submittedAt(review.getSubmittedAt())
        .author(author)
        .body(review.getBody())
        .build();
  }
}
