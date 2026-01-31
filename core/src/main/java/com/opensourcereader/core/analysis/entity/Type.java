package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.entity.type.TypeOrigin;
import com.opensourcereader.core.shared.BaseEntity;
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
public class Type extends BaseEntity {

  @Column(name = "class_internal_name")
  private String typeInternalName;

  @Enumerated(EnumType.STRING)
  private TypeKind typeKind;

  @OneToOne(cascade = CascadeType.MERGE)
  @JoinColumn(name = "super_type_id")
  private Type superType;

  @OneToMany(
      mappedBy = "implementedType",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<TypeImplementation> implementedInterfaces;

  @OneToMany(
      mappedBy = "interfaceType",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<TypeImplementation> implementations;

  @OneToMany(
      mappedBy = "type",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<Method> methods;

  @Enumerated(EnumType.STRING)
  private TypeOrigin typeOrigin;

  @OneToOne
  @JoinColumn(name = "repo_content_id")
  private OpenSourceRepoContent openSourceRepoContent;

  static Type internal(
      TypeInfo typeInfo,
      List<DeclaredMethodInfo> declaredMethodInfos,
      OpenSourceRepoContent openSourceRepoContent) {
    if (typeInfo == null) {
      return null;
    }
    return new Type(typeInfo, declaredMethodInfos, TypeOrigin.INTERNAL, openSourceRepoContent);
  }

  static Type external(String typeInternalName, OpenSourceRepoContent openSourceRepoContent) {
    return new Type(typeInternalName, null, TypeOrigin.EXTERNAL, openSourceRepoContent, null);
  }

  private Type(
      String typeInternalName,
      TypeKind typeKind,
      TypeOrigin typeOrigin,
      OpenSourceRepoContent openSourceRepoContent,
      List<Method> methods) {
    this.typeInternalName = typeInternalName;
    this.typeKind = typeKind;
    this.typeOrigin = typeOrigin;
    this.openSourceRepoContent = openSourceRepoContent;
    this.methods = methods;
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
  }

  private Type(
      TypeInfo typeInfo,
      List<DeclaredMethodInfo> methodExtractResults,
      TypeOrigin typeOrigin,
      OpenSourceRepoContent openSourceRepoContent) {
    this.typeInternalName = extractedTypeName(typeInfo);
    this.typeKind = typeInfo.typeKind();
    this.methods = getCodeMethods(methodExtractResults);
    this.typeOrigin = typeOrigin;
    this.openSourceRepoContent = openSourceRepoContent;
    this.implementedInterfaces = new ArrayList<>();
    this.implementations = new ArrayList<>();
  }

  private String extractedTypeName(TypeInfo typeInfo) {
    if (typeInfo == null) {
      return null;
    }
    return typeInfo.internalName();
  }

  private List<Method> getCodeMethods(List<DeclaredMethodInfo> methodExtractResults) {
    return methodExtractResults.stream()
        .map(extractResult -> Method.declared(extractResult, this))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public void addInheritedVirtualMethod(Method superMethod) {
    if (superMethod == null) {
      return;
    }
    if (this.superType == null) {
      return;
    }
    Method inheritedVirtual = createInheritedVirtual(superMethod);
    if (methods.contains(inheritedVirtual)) {
      return;
    }
    methods.add(inheritedVirtual);
    superMethod.addOutgoingCall(inheritedVirtual);
  }

  private Method createInheritedVirtual(Method superMethod) {
    if (this.superType.isInternal()) {
      return Method.inheritedInternal(superMethod, this);
    }
    return Method.inheritedExternal(superMethod, this);
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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Type that)) {
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
