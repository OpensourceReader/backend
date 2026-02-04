package com.opensourcereader.core.analysis.domain.entity;

import java.util.Objects;

import com.opensourcereader.core.analysis.domain.entity.file.Extension;
import com.opensourcereader.core.analysis.domain.entity.file.OpenSourceRepoFileName;
import com.opensourcereader.core.analysis.domain.entity.file.OpenSourceRepoFileOrigin;
import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoFile extends BaseEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Embedded
  @Column(name = "name", nullable = false)
  private OpenSourceRepoFileName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "repo_file_type", nullable = false)
  private RepoFileType repoFileType;

  @Enumerated(EnumType.STRING)
  @Column(name = "extension")
  private Extension extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @Enumerated(EnumType.STRING)
  @Column(name = "origin")
  private OpenSourceRepoFileOrigin origin;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repo_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  static OpenSourceRepoFile internalDeclared(
      TypeStructure typeStructure, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoFile(
        typeStructure.path(),
        typeStructure.repoFileType(),
        OpenSourceRepoFileOrigin.INTERNAL,
        typeStructure.rawText(),
        openSourceRepo);
  }

  private OpenSourceRepoFile(
      String path,
      RepoFileType repoFileType,
      OpenSourceRepoFileOrigin origin,
      String rawText,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoFileName.from(path);
    this.origin = origin;
    this.repoFileType = repoFileType;
    this.rawText = rawText;
    this.openSourceRepo = openSourceRepo;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OpenSourceRepoFile that)) {
      return false;
    }
    return Objects.equals(path, that.path) && Objects.equals(openSourceRepo, that.openSourceRepo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, openSourceRepo);
  }
}
