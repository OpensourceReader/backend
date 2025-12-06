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
public class OpensourceRepoContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpensourceRepoContentName name;

  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "content", nullable = false)
  private String rawText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpensourceRepo opensourceRepo;

  public static OpensourceRepoContent of(
      GitTreeFileInfo fileInfo,
      String rawText,
      OpensourceRepo opensourceRepo
  ) {
    return new OpensourceRepoContent(
        fileInfo.path(),
        fileInfo.url(),
        fileInfo.type(),
        rawText,
        opensourceRepo
    );
  }

  private OpensourceRepoContent(
      String path,
      String url,
      String fileInfoType,
      String rawText,
      OpensourceRepo opensourceRepo
  ) {
    this.path = path;
    this.url = url;
    this.name = OpensourceRepoContentName.from(path);
    this.contentType = ContentType.getContentType(fileInfoType);
    this.rawText = rawText;
    this.opensourceRepo = opensourceRepo;
  }

}
