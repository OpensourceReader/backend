package com.opensourcereader.core.issue.entity;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "labels",
    indexes = {
      @Index(name = "idx_label_repository_id", columnList = "repository_id"),
    })
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Label {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "repository_id", nullable = false)
  private OpenSourceRepo repository;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id", nullable = false)
  private Issue issue;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String colorCode;

  @Column private String description;

  public static LabelBuilder of(
      OpenSourceRepo repository, Issue issue, String name, String colorCode) {
    return Label.builder().repository(repository).issue(issue).name(name).colorCode(colorCode);
  }
}
