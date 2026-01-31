package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.content.Extension;
import com.opensourcereader.core.analysis.entity.content.OpenSourceRepoContentName;
import com.opensourcereader.core.analysis.entity.content.OpenSourceRepoContentOrigin;
import com.opensourcereader.core.analysis.entity.content.RepoEntryType;
import com.opensourcereader.core.shared.BaseEntity;
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
  @Column(name = "repo_entry_type", nullable = false)
  private RepoEntryType repoEntryType;

  @Enumerated(EnumType.STRING)
  @Column(name = "extension")
  private Extension extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @Enumerated(EnumType.STRING)
  private OpenSourceRepoContentOrigin origin;

  @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private Type type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  static OpenSourceRepoContent internal(
      TypeStructure typeStructure, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        typeStructure.path(),
        typeStructure.repoEntryType(),
        OpenSourceRepoContentOrigin.INTERNAL,
        typeStructure.rawText(),
        typeStructure.typeInfo(),
        typeStructure.methods().stream()
            .map(MethodStructure::methodInfo)
            .collect(Collectors.toCollection(ArrayList::new)),
        openSourceRepo);
  }

  static OpenSourceRepoContent external(String typeInternalName, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        typeInternalName,
        typeInternalName + UUID.randomUUID(),
        RepoEntryType.OTHERS,
        OpenSourceRepoContentOrigin.EXTERNAL,
        null,
        openSourceRepo);
  }

  static OpenSourceRepoContent external(
      MethodCallInfo calleeMethodInfo, Method callerMethod, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(
        calleeMethodInfo,
        calleeMethodInfo.typeInternalName() + UUID.randomUUID(),
        RepoEntryType.OTHERS,
        OpenSourceRepoContentOrigin.EXTERNAL,
        null,
        callerMethod,
        openSourceRepo);
  }

  private OpenSourceRepoContent(
      MethodCallInfo calleeMethodInfo,
      String path,
      RepoEntryType repoEntryType,
      OpenSourceRepoContentOrigin origin,
      String rawText,
      Method caller,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.origin = origin;
    this.repoEntryType = repoEntryType;
    this.rawText = rawText;
    this.type = Type.external(calleeMethodInfo, caller, this);
    this.openSourceRepo = openSourceRepo;
  }

  private OpenSourceRepoContent(
      String typeInternalName,
      String path,
      RepoEntryType repoEntryType,
      OpenSourceRepoContentOrigin origin,
      String rawText,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.origin = origin;
    this.repoEntryType = repoEntryType;
    this.rawText = rawText;
    this.type = Type.external(typeInternalName, this);
    this.openSourceRepo = openSourceRepo;
  }

  private OpenSourceRepoContent(
      String path,
      RepoEntryType repoEntryType,
      OpenSourceRepoContentOrigin origin,
      String rawText,
      TypeInfo typeInfo,
      List<MethodInfo> methodInfoWithSources,
      OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = Extension.resolveExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.origin = origin;
    this.repoEntryType = repoEntryType;
    this.rawText = rawText;
    this.type = Type.internal(typeInfo, methodInfoWithSources, this);
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
