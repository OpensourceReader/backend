package com.opensourcereader.core.analysis.entity.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
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
  private List<DeclaredTypeImplementEdge> implementedInterfaces;

  @OneToMany(
      mappedBy = "interfaceType",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<DeclaredTypeImplementEdge> implementations;

  @OneToMany(
      mappedBy = "declaredType",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<DeclaredMethod> declaredMethods;

  @Enumerated(EnumType.STRING)
  private TypeOrigin typeOrigin;

  @OneToOne
  @JoinColumn(name = "repo_content_id")
  private OpenSourceRepoContent openSourceRepoContent;

  public static DeclaredType internal(
      ClassInfo classInfo,
      List<CodeMethodExtractResult> methodExtractResults,
      OpenSourceRepoContent openSourceRepoContent) {
    if (classInfo == null) {
      return null;
    }
    return new DeclaredType(
        classInfo, methodExtractResults, TypeOrigin.INTERNAL, openSourceRepoContent);
  }

  public static DeclaredType external(String typeInternalName) {
    return new DeclaredType(typeInternalName, null, TypeOrigin.EXTERNAL, null, null);
  }

  public void updateMethod(DeclaredMethod newMethod) {
    if (newMethod == null) {
      return;
    }
    if (declaredMethods.contains(newMethod)) {
      return;
    }
    declaredMethods.add(newMethod);
  }

  public void update(DeclaredType newSuperType, List<DeclaredTypeImplementEdge> newImplementEdges) {
    if (newSuperType != null) {
      this.superType = newSuperType;
    }
    syncImplementedInterfaceEdges(newImplementEdges);
  }

  private void syncImplementedInterfaceEdges(List<DeclaredTypeImplementEdge> newEdges) {
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
        newEdge.getInterfaceType().updateImplementation(newEdge);
      }
    }
  }

  private void updateImplementation(DeclaredTypeImplementEdge newEdge) {
    if (newEdge == null) {
      return;
    }
    if (this.implementations.contains(newEdge)) {
      return;
    }

    this.implementations.add(newEdge);
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
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
  }

  private DeclaredType(
      ClassInfo classInfo,
      List<CodeMethodExtractResult> methodExtractResults,
      TypeOrigin typeOrigin,
      OpenSourceRepoContent openSourceRepoContent) {
    this.typeInternalName = extractedTypeName(classInfo);
    this.typeKind = classInfo.typeKind();
    this.declaredMethods = getCodeMethods(methodExtractResults);
    this.typeOrigin = typeOrigin;
    this.openSourceRepoContent = openSourceRepoContent;
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
  }

  private String extractedTypeName(ClassInfo classInfo) {
    if (classInfo == null) {
      return null;
    }
    return classInfo.className();
  }

  private List<DeclaredMethod> getCodeMethods(List<CodeMethodExtractResult> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> DeclaredMethod.internal(extractResult, this))
        .collect(Collectors.toCollection(ArrayList::new));
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
