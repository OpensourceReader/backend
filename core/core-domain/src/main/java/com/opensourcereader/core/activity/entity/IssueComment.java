package com.opensourcereader.core.activity.entity;

import java.time.Instant;

import com.opensourcereader.core.activity.dto.IssueCommentCommand;
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
@Table(name = "issue_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "provider_id")
  private Long providerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  @Column(columnDefinition = "LONGTEXT")
  private String body;

  private IssueComment(IssueCommentCommand command) {
    this.providerId = command.getId();
    this.createdAt = command.getCreatedAt();
    this.updatedAt = command.getUpdatedAt();
    this.author = command.getAuthor();
    this.issue = command.getIssue();
    this.body = command.getBody();
  }

  public static IssueComment from(IssueCommentCommand command) {
    return new IssueComment(command);
  }
}
