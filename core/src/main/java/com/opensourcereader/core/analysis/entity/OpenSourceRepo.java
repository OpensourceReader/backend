package com.opensourcereader.core.analysis.entity;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepo extends BaseEntity {

  @Column(name = "clone_url")
  private String cloneUrl;

  @OneToMany(
      mappedBy = "openSourceRepo",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<OpenSourceRepoContent> contents = new ArrayList<>();

  public static OpenSourceRepo of(String cloneUrl, List<TypeStructure> typeStructures) {
    return new OpenSourceRepo(cloneUrl, typeStructures);
  }

  private OpenSourceRepo(String cloneUrl, List<TypeStructure> typeStructures) {
    this.cloneUrl = cloneUrl;
    this.contents =
        typeStructures.stream()
            .map(structure -> OpenSourceRepoContent.internal(structure, this))
            .collect(Collectors.toCollection(ArrayList::new));
  }

  // ingoing으로 넣어주기
  public void addExternalMethod(MethodCallInfo calleeMethodInfo, Method callerMethod) {
    OpenSourceRepoContent newContent =
        OpenSourceRepoContent.external(calleeMethodInfo, callerMethod, this);
    if (this.contents.contains(newContent)) {
      return;
    }
    contents.add(newContent);
  }

  public void addExternalType(String typeInternalName) {
    OpenSourceRepoContent newContent = OpenSourceRepoContent.external(typeInternalName, this);
    if (this.contents.contains(newContent)) {
      return;
    }
    contents.add(newContent);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OpenSourceRepo that)) {
      return false;
    }
    return Objects.equals(cloneUrl, that.cloneUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(cloneUrl);
  }
}
