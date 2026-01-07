package com.opensourcereader.core.analysis.entity.repo;

import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContent extends BaseEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Embedded
  @Column(name = "name", nullable = false)
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private RepoEntryType repoEntryType;

  @Enumerated(EnumType.STRING)
  @Column(name = "extension")
  private Extension extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @OneToOne
  @JoinColumn(name = "declared_type_id")
  private DeclaredType declaredType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  public static OpenSourceRepoContent of(
      OpenSourceFileInfo fileInfo,
      ClassStructure classStructure,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        fileInfo.path(),
        fileInfo.repoEntryType(),
        fileInfo.rawText(),
        classStructure,
        methodExtractResults,
        openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path,
      RepoEntryType repoEntryType,
      String rawText,
      ClassStructure classStructure,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.repoEntryType = repoEntryType;
    this.rawText = rawText;
    this.declaredType = DeclaredType.internal(classStructure, methodExtractResults);
    this.openSourceRepo = openSourceRepo;
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
