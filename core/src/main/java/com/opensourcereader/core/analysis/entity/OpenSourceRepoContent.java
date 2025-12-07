package com.opensourcereader.core.analysis.entity;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
public class OpenSourceRepoContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpenSourceRepoContentName name;

  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "content")
  private String rawText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo opensourceRepo;

  public static OpenSourceRepoContent of(
      GitTreeFileInfo fileInfo,
      String rawText,
      OpenSourceRepo opensourceRepo
  ) {
    return new OpenSourceRepoContent(
        fileInfo.path(),
        fileInfo.type(),
        rawText,
        opensourceRepo
    );
  }

  private OpenSourceRepoContent(
      String path,
      ContentType contentType,
      String rawText,
      OpenSourceRepo opensourceRepo
  ) {
    this.path = path;
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = contentType;
    this.rawText = rawText;
    this.opensourceRepo = opensourceRepo;
  }

}
