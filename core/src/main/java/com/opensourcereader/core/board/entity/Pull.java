package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pulls")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Pull extends BaseEntity {

  @Column(name = "tag_id", nullable = false)
  private Long tagId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User user;

  // 세컨드 인덱스 : repository_id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "repository_id", nullable = false)
  private OpenSourceRepo repository;

  @Column(nullable = false)
  private String title;

  @Lob @Column private String body;

  @Column(name = "is_opened", nullable = false)
  private Boolean isOpened;

  @Column(name = "review_count", nullable = false)
  private Long reviewCount;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  public static PullBuilder of(
      Long tagId,
      User author,
      OpenSourceRepo repository,
      String title,
      Boolean isOpened,
      Long reviewCount,
      Long commentCount) {
    return Pull.builder()
        .tagId(tagId)
        .user(author)
        .repository(repository)
        .title(title)
        .isOpened(isOpened)
        .reviewCount(reviewCount)
        .commentCount(commentCount);
  }

  // 대량의 변화일 가능성이 높기에 통짜로 변경한다.
  // TODO 단, 로그를 남겨야 한다.(이전 변화, 현재 변화)
  public void updateBody(String newContent) {
    this.body = newContent;
  }

  public void updateStatus(Boolean newStatus) {
    this.isOpened = updateField(this.isOpened, newStatus);
  }

  public void updateReviewCount(Long newReviewCount) {
    this.reviewCount = updateField(this.reviewCount, newReviewCount);
  }

  public void updateCommentCount(Long newCommentCount) {
    this.commentCount = updateField(this.commentCount, newCommentCount);
  }

  private <T> T updateField(T target, T replace) {
    if (target == null && replace != null) {
      return replace;
    }
    if (target != null && replace != null && !target.equals(replace)) {
      return replace;
    }
    return target;
  }
}
