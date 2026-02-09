package com.opensourcereader.core.collaboration.entity;

import java.time.Instant;

import com.opensourcereader.core.collaboration.dto.PullCommentCommand;
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
@Table(name = "pull_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(name = "diff_hunk", nullable = false)
  private String diffHunk;

  @Column(nullable = false)
  private String body;

  @Column(nullable = false)
  private String path;

  private PullComment(PullCommentCommand command) {
    this.createdAt = command.getCreatedAt();
    this.updatedAt = command.getUpdatedAt();
    this.author = command.getAuthor();
    this.review = command.getReview();
    this.diffHunk = command.getDiffHunk();
    this.body = command.getBody();
    this.path = command.getPath();
  }

  public static PullComment from(PullCommentCommand command) {
    return new PullComment(command);
  }
}
