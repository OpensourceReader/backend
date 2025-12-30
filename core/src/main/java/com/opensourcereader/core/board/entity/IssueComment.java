package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.board.dto.IssueCommentCommand;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "IssueComments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueComment extends BaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  @Column(nullable = false)
  private String body;

  @Column(nullable = false)
  private Boolean disabled = false;

  private IssueComment(IssueCommentCommand command) {
    super(command.getId(), command.getCreatedAt(), command.getUpdatedAt());
    this.author = command.getAuthor();
    this.issue = command.getIssue();
    this.body = command.getBody();
  }

  public static IssueComment from(IssueCommentCommand command) {
    return new IssueComment(command);
  }

  public void updateDisabled(Boolean newDisabled) {
    this.disabled = newDisabled;
  }
}
