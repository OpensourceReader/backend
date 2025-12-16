package com.opensourcereader.core.board.dto;

import java.time.temporal.ChronoUnit;

import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record ReviewDto(
    Long reviewId, String createdAt, String updatedAt, UserDto reviewAuthor, String body) {

  public static ReviewDto of(Review review, UserDto reviewAuthor) {
    // time format : yyyy-MM-ddTHH:mm:ss.sssZ
    String createdAtToString = review.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).toString();
    String updatedAtToString = review.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS).toString();

    return ReviewDto.builder()
        .reviewId(review.getId())
        .createdAt(createdAtToString)
        .updatedAt(updatedAtToString)
        .reviewAuthor(reviewAuthor)
        .body(review.getBody())
        .build();
  }
}
