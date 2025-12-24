package com.opensourcereader.core.analysis.entity;

import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.OpenSourceContentMethodExtractResult;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_code_open_source_repo_content",
          columnNames = {"path"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContent extends BaseEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  // enum 수정필요
  @Column(name = "extension")
  private String extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "opensource_repo_content_id")
  private List<CodeMethodMetaData> codeMethodMetaData;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  public static OpenSourceRepoContent of(
      OpenSourceFileInfo fileInfo,
      List<OpenSourceContentMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        fileInfo.path(), fileInfo.type(), fileInfo.rawText(), methodExtractResults, openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path,
      String contentTypeNumber,
      String rawText,
      List<OpenSourceContentMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = OpenSourceRepoContentName.getExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = ContentType.getContentTypeFromTypeNumber(contentTypeNumber);
    this.rawText = rawText;
    this.codeMethodMetaData = getCodeMethodMetaData(methodExtractResults);
    this.openSourceRepo = openSourceRepo;
  }

  private List<CodeMethodMetaData> getCodeMethodMetaData(
      List<OpenSourceContentMethodExtractResult> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> CodeMethodMetaData.of(extractResult, this))
        .toList();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OpenSourceRepoContent that)) {
      return false;
    }
    return Objects.equals(path, that.path) && Objects.equals(openSourceRepo, that.openSourceRepo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, openSourceRepo);
  }
}
