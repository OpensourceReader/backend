package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.board.dto.PullCommand;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("PULL")
public class Pull extends Issue {

  @Column(name = "review_count")
  private Integer reviewCount;

  private Pull(PullCommand command) {
    super(command);
    this.reviewCount = command.getReviewCount();
  }

  public static Pull from(PullCommand command) {
    return new Pull(command);
  }

  public void updateReviewCount(Integer newReviewCount) {
    this.reviewCount = updateField(this.reviewCount, newReviewCount);
  }
}
