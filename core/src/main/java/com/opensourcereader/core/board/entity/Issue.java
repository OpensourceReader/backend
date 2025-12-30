package com.opensourcereader.core.board.entity;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.board.dto.BoardBaseCommand;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "issues")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "provider_id")
  private Long providerId;

  @Column(name = "tag_id")
  private Long tagId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "repository_id", nullable = false)
  private OpenSourceRepo repository;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "LONGTEXT")
  private String body;

  @Column(name = "is_opened", nullable = false)
  private Boolean isOpened;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @Column(nullable = false)
  private Boolean disabled = false;

  protected Issue(BoardBaseCommand command) {
    this.providerId = command.getId();
    this.createdAt = command.getCreatedAt();
    this.updatedAt = command.getUpdatedAt();
    this.tagId = command.getTagId();
    this.author = command.getAuthor();
    this.repository = command.getRepo();
    this.title = command.getTitle();
    this.body = command.getBody();
    this.isOpened = command.getIsOpened();
    this.commentCount = command.getCommentCount();
  }

  public static Issue from(BoardBaseCommand command) {
    return new Issue(command);
  }

  // 대량의 변화일 가능성이 높기에 통짜로 변경한다.
  // TODO 로그를 남겨야 한다.(이전 변화, 현재 변화)
  public void updateBody(String newContent) {
    this.body = newContent;
  }

  public void updateDisabled(Boolean newDisabled) {
    this.disabled = newDisabled;
  }

  public void updateStatus(Boolean newStatus) {
    this.isOpened = updateField(this.isOpened, newStatus);
  }

  public void updateCommentCount(Long newCommentCount) {
    this.commentCount = updateField(this.commentCount, newCommentCount);
  }

  protected <T> T updateField(T target, T replace) {
    if (target == null && replace != null) {
      return replace;
    }
    if (target != null && replace != null && !target.equals(replace)) {
      return replace;
    }
    return target;
  }
}
