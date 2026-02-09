package com.opensourcereader.core.analysis.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.entity.type.TypeOrigin;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.external.ExternalMethodInfo;
import com.opensourcereader.core.analysis.dto.external.ExternalTypeStructure;
import com.opensourcereader.core.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Type extends BaseEntity {

  @Column(name = "type_internal_name", nullable = false)
  private String typeInternalName;

  @Enumerated(EnumType.STRING)
  @Column(name = "type_kind")
  private TypeKind typeKind;

  @OneToOne
  @JoinColumn(name = "super_type_id")
  private Type superType;

  @Enumerated(EnumType.STRING)
  @Column(name = "origin", nullable = false)
  private TypeOrigin typeOrigin;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repo_file_id", nullable = true)
  private OpenSourceRepoFile openSourceRepoFile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repo_id")
  private OpenSourceRepo openSourceRepo;

  @OneToMany(mappedBy = "type", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<Method> methods;

  @OneToMany(mappedBy = "implementedType", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<TypeImplementation> implementedInterfaces;

  @OneToMany(mappedBy = "interfaceType", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  private List<TypeImplementation> implementations;

  static Type internalDeclared(
      TypeInfo typeInfo,
      List<MethodInfo> methodInfos,
      OpenSourceRepoFile openSourceRepoFile,
      OpenSourceRepo openSourceRepo) {
    if (typeInfo == null) {
      return null;
    }
    return new Type(typeInfo, methodInfos, TypeOrigin.INTERNAL, openSourceRepoFile, openSourceRepo);
  }

  static Type external(ExternalTypeStructure externalTypeStructure, OpenSourceRepo openSourceRepo) {
    return new Type(
        externalTypeStructure.externalTypeInfo().typeInternalName(),
        null,
        TypeOrigin.EXTERNAL,
        null,
        openSourceRepo,
        externalTypeStructure.externalMethodInfos());
  }

  private Type(
      String typeInternalName,
      TypeKind typeKind,
      TypeOrigin typeOrigin,
      OpenSourceRepoFile openSourceRepoFile,
      OpenSourceRepo openSourceRepo,
      List<ExternalMethodInfo> externalMethodInfos) {
    this.typeInternalName = typeInternalName;
    this.typeKind = typeKind;
    this.typeOrigin = typeOrigin;
    this.openSourceRepoFile = openSourceRepoFile;
    this.methods = new ArrayList<>(Method.external(externalMethodInfos, this));
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
    this.openSourceRepo = openSourceRepo;
  }

  private Type(
      TypeInfo typeInfo,
      List<MethodInfo> methodInfos,
      TypeOrigin typeOrigin,
      OpenSourceRepoFile openSourceRepoFile,
      OpenSourceRepo openSourceRepo) {
    this.typeInternalName = extractedTypeName(typeInfo);
    this.typeKind = typeInfo.typeKind();
    this.typeOrigin = typeOrigin;
    this.openSourceRepoFile = openSourceRepoFile;
    this.methods = new ArrayList<>(getInternalMethods(methodInfos));
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
    this.openSourceRepo = openSourceRepo;
  }

  private String extractedTypeName(TypeInfo typeInfo) {
    if (typeInfo == null) {
      return null;
    }
    return typeInfo.typeInternalName();
  }

  private List<Method> getInternalMethods(List<MethodInfo> methodInfos) {
    return methodInfos.stream()
        .map(methodInfo -> Method.internalDeclared(methodInfo, this))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public void addExternalInheritanceMethod(Method externalInheritedMethod) {
    if (externalInheritedMethod == null) {
      return;
    }
    if (methods.contains(externalInheritedMethod)) {
      return;
    }
    methods.add(externalInheritedMethod);
  }

  void addVirtualMethod(Method inheritedVirtual) {
    if (inheritedVirtual == null) {
      return;
    }
    if (methods.contains(inheritedVirtual)) {
      return;
    }
    methods.add(inheritedVirtual);
  }

  public void updateRelations(Type newSuperType, List<Type> interfaceTypes) {
    if (newSuperType != null) {
      this.superType = newSuperType;
    }
    List<TypeImplementation> newInterfaceEdges =
        interfaceTypes.stream()
            .map(interfaceType -> TypeImplementation.of(this, interfaceType))
            .toList();
    syncImplementedInterfaceEdges(newInterfaceEdges);
  }

  private void syncImplementedInterfaceEdges(List<TypeImplementation> newEdges) {
    if (newEdges == null) {
      return;
    }

    this.implementedInterfaces.removeIf(
        existing -> newEdges.stream().noneMatch(ne -> sameInterface(existing, ne)));

    for (TypeImplementation newEdge : newEdges) {
      boolean exists =
          this.implementedInterfaces.stream()
              .anyMatch(existing -> sameInterface(existing, newEdge));
      if (exists) {
        continue;
      }
      this.implementedInterfaces.add(newEdge);
      newEdge.getInterfaceType().updateImplementation(newEdge);
    }
  }

  private void updateImplementation(TypeImplementation newEdge) {
    if (newEdge == null) {
      return;
    }
    if (this.implementations.contains(newEdge)) {
      return;
    }

    this.implementations.add(newEdge);
  }

  private boolean sameInterface(TypeImplementation a, TypeImplementation b) {
    return a.getInterfaceType()
        .getTypeInternalName()
        .equals(b.getInterfaceType().getTypeInternalName());
  }

  public boolean isInternal() {
    if (this.typeOrigin == null) {
      return false;
    }
    return this.typeOrigin.equals(TypeOrigin.INTERNAL);
  }

  public boolean isSameTypeKind(TypeKind kind) {
    if (kind == null || this.getTypeKind() == null) {
      return false;
    }
    return this.getTypeKind().equals(kind);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Type that)) {
      return false;
    }
    return Objects.equals(typeInternalName, that.typeInternalName)
        && typeOrigin == that.typeOrigin
        && Objects.equals(openSourceRepo, that.openSourceRepo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeInternalName, typeOrigin, openSourceRepo);
  }
}
