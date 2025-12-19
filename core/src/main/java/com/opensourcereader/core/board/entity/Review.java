package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.board.dto.ReviewCommand;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_id", nullable = false)
  private Pull pull;

  @Column(nullable = false)
  private String body;

  @Column(nullable = false)
  private Boolean disabled = false;

  private Review(ReviewCommand command) {
    super(command.getId(), command.getCreatedAt(), command.getUpdatedAt());
    this.author = command.getAuthor();
    this.pull = command.getPull();
    this.body = command.getBody();
  }

  public static Review from(ReviewCommand command) {
    return new Review(command);
  }

  public void updateDisabled(Boolean newDisabled) {
    this.disabled = newDisabled;
  }
}
