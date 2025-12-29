package com.opensourcereader.core.analysis.entity;

import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContent extends BaseEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "class_internal_name", nullable = false)
  private String classInternalName;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "extension")
  @Enumerated(EnumType.STRING)
  private Extension extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @OneToMany(
      mappedBy = "openSourceRepoContent",
      fetch = FetchType.LAZY,
      cascade = CascadeType.PERSIST)
  private List<CodeMethod> codeMethods;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  public static OpenSourceRepoContent of(
      OpenSourceFileInfo fileInfo,
      ClassInfo classInfo,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        fileInfo.path(),
        fileInfo.typeNumber(),
        fileInfo.rawText(),
        classInfo,
        methodExtractResults,
        openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path,
      String contentTypeNumber,
      String rawText,
      ClassInfo classInfo,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = ContentType.getContentTypeFromTypeNumber(contentTypeNumber);
    this.rawText = rawText;
    this.classInternalName = classInfo.className();
    this.codeMethods = getCodeMethods(methodExtractResults);
    this.openSourceRepo = openSourceRepo;
  }

  private List<CodeMethod> getCodeMethods(List<CodeMethodExtractResult> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> CodeMethod.of(extractResult, this))
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
