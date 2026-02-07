package com.opensourcereader.core.collaboration.entity;

import java.time.Instant;

import com.opensourcereader.core.collaboration.dto.ReviewCommand;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long providerId;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_id", nullable = false)
  private Pull pull;

  @Column(nullable = false)
  private String body;

  private Review(ReviewCommand command) {
    this.providerId = command.id();
    this.submittedAt = command.submittedAt();
    this.author = command.author();
    this.pull = command.pull();
    this.body = command.body();
  }

  public static Review from(ReviewCommand command) {
    return new Review(command);
  }
}
