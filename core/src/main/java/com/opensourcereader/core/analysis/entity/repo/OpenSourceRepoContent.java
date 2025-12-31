package com.opensourcereader.core.analysis.entity.repo;

import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;
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

  @Column(name = "class_internal_name")
  private String classInternalName;

  @Embedded
  @Column(name = "name", nullable = false)
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "extension")
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
      ClassStructure classStructure,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        fileInfo.path(),
        fileInfo.contentType(),
        fileInfo.rawText(),
        classStructure,
        methodExtractResults,
        openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path,
      ContentType contentType,
      String rawText,
      ClassStructure classStructure,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = contentType;
    this.rawText = rawText;
    this.classInternalName = extractedClassName(classStructure);
    this.codeMethods = getCodeMethods(methodExtractResults);
    this.openSourceRepo = openSourceRepo;
  }

  private String extractedClassName(ClassStructure classStructure) {
    if (classStructure == null) {
      return null;
    }
    return classStructure.classInfo().className();
  }

  private List<CodeMethod> getCodeMethods(List<CodeMethodExtractResult> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> CodeMethod.internal(extractResult, this))
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
