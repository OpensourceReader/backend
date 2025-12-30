package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.board.dto.PullCommentCommand;
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
@Table(name = "PullComments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullComment extends BaseEntity {
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

  @Column(nullable = false)
  private Boolean disabled = false;

  private PullComment(PullCommentCommand command) {
    super(command.getId(), command.getCreatedAt(), command.getUpdatedAt());
    this.author = command.getAuthor();
    this.review = command.getReview();
    this.diffHunk = command.getDiffHunk();
    this.body = command.getBody();
    this.path = command.getPath();
  }

  public static PullComment from(PullCommentCommand command) {
    return new PullComment(command);
  }

  public void updateDisabled(Boolean newDisabled) {
    this.disabled = newDisabled;
  }
}
