package com.opensourcereader.core.analysis.entity.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeclaredType extends BaseEntity {

  @Column(name = "class_internal_name")
  private String typeInternalName;

  @Enumerated(EnumType.STRING)
  private TypeKind typeKind;

  @OneToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "super_type_id")
  private DeclaredType superType;

  @OneToMany(
      mappedBy = "type",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private final List<DeclaredTypeImplementEdge> implementedInterfaces = new ArrayList<>();

  @OneToMany(
      mappedBy = "interfaceType",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private final List<DeclaredTypeImplementEdge> implementations = new ArrayList<>();

  @OneToMany(mappedBy = "declaredType", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<DeclaredMethod> declaredMethods;

  @Enumerated(EnumType.STRING)
  private TypeOrigin typeOrigin;

  @OneToOne
  @JoinColumn(name = "repo_content_id")
  private OpenSourceRepoContent openSourceRepoContent;

  public static DeclaredType internal(
      ClassStructure classStructure, List<CodeMethodExtractResult> methodExtractResults) {
    if (classStructure == null) {
      return null;
    }
    return new DeclaredType(classStructure, methodExtractResults, TypeOrigin.INTERNAL);
  }

  public static DeclaredType external(String typeInternalName) {
    return new DeclaredType(typeInternalName, null, TypeOrigin.EXTERNAL, null, null);
  }

  public void update(DeclaredType newSuperType, List<DeclaredTypeImplementEdge> newImplementEdges) {
    if (newSuperType != null) {
      this.superType = newSuperType;
    }
    syncInterfaceEdges(newImplementEdges);
  }

  private void syncInterfaceEdges(List<DeclaredTypeImplementEdge> newEdges) {
    if (newEdges == null) {
      return;
    }

    this.implementedInterfaces.removeIf(
        existing -> newEdges.stream().noneMatch(ne -> sameInterface(existing, ne)));

    for (DeclaredTypeImplementEdge newEdge : newEdges) {
      boolean exists =
          this.implementedInterfaces.stream()
              .anyMatch(existing -> sameInterface(existing, newEdge));

      if (!exists) {
        this.implementedInterfaces.add(newEdge);
      }
    }
  }

  private boolean sameInterface(DeclaredTypeImplementEdge a, DeclaredTypeImplementEdge b) {
    return a.getInterfaceType()
        .getTypeInternalName()
        .equals(b.getInterfaceType().getTypeInternalName());
  }

  private DeclaredType(
      String typeInternalName,
      TypeKind typeKind,
      TypeOrigin typeOrigin,
      OpenSourceRepoContent openSourceRepoContent,
      List<DeclaredMethod> declaredMethods) {
    this.typeInternalName = typeInternalName;
    this.typeKind = typeKind;
    this.typeOrigin = typeOrigin;
    this.openSourceRepoContent = openSourceRepoContent;
    this.declaredMethods = declaredMethods;
  }

  private DeclaredType(
      ClassStructure classStructure,
      List<CodeMethodExtractResult> methodExtractResults,
      TypeOrigin typeOrigin) {
    this.typeInternalName = extractedTypeName(classStructure);
    this.declaredMethods = getCodeMethods(methodExtractResults);
    this.typeOrigin = typeOrigin;
  }

  private String extractedTypeName(ClassStructure classStructure) {
    if (classStructure == null) {
      return null;
    }
    return classStructure.classInfo().className();
  }

  private List<DeclaredMethod> getCodeMethods(List<CodeMethodExtractResult> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> DeclaredMethod.internal(extractResult, this))
        .toList();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DeclaredType that)) {
      return false;
    }
    return Objects.equals(typeInternalName, that.typeInternalName)
        && typeOrigin == that.typeOrigin
        && Objects.equals(openSourceRepoContent, that.openSourceRepoContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeInternalName, typeOrigin, openSourceRepoContent);
  }
}
