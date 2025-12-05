package com.opensourcereader.core.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "content", nullable = false)
  private String rawText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpensourceRepository opensourceRepository;

  public RepositoryContent(
      String name,
      String path,
      ContentType contentType,
      String rawText,
      OpensourceRepository opensourceRepository
  ) {
    this.name = name;
    this.path = path;
    this.contentType = contentType;
    this.rawText = rawText;
    this.opensourceRepository = opensourceRepository;
  }

}
