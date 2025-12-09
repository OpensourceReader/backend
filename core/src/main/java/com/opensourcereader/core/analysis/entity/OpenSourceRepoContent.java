package com.opensourcereader.core.analysis.entity;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  public static OpenSourceRepoContent of(
      GitTreeFileInfo fileInfo, String rawText, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(fileInfo.path(), fileInfo.type(), rawText, openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path, ContentType contentType, String rawText, OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = contentType;
    this.rawText = rawText;
    this.openSourceRepo = openSourceRepo;
  }
}
