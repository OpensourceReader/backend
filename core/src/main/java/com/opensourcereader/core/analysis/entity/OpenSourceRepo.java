package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.dto.ExternalTypeInfo;
import com.opensourcereader.core.analysis.dto.ExternalTypeStructure;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
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

  @Getter(AccessLevel.NONE)
  @OneToMany(mappedBy = "openSourceRepo", cascade = CascadeType.PERSIST)
  private List<OpenSourceRepoFile> files = new ArrayList<>();

  @Getter(AccessLevel.NONE)
  @OneToMany(mappedBy = "openSourceRepo", cascade = CascadeType.PERSIST)
  private List<Type> types = new ArrayList<>();

  public static OpenSourceRepo of(String cloneUrl, List<TypeStructure> typeStructures) {
    return new OpenSourceRepo(cloneUrl, typeStructures);
  }

  private OpenSourceRepo(String cloneUrl, List<TypeStructure> typeStructures) {
    this.cloneUrl = cloneUrl;
    this.files = createOpenSourceRepoFiles(typeStructures);
    this.types = createTypes(this.files, typeStructures);
    this.addExternalTypes(this.types, typeStructures);
  }

  private List<OpenSourceRepoFile> createOpenSourceRepoFiles(List<TypeStructure> typeStructures) {
    return typeStructures.stream()
        .map(structure -> OpenSourceRepoFile.internalDeclared(structure, this))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<Type> createTypes(
      List<OpenSourceRepoFile> files, List<TypeStructure> typeStructures) {
    Map<String, TypeStructure> typeStructureByPaths =
        typeStructures.stream()
            .collect(Collectors.toMap(TypeStructure::path, typeStructure -> typeStructure));

    return files.stream()
        .map(
            file -> {
              TypeStructure typeStructure = typeStructureByPaths.get(file.getPath());
              if (typeStructure == null) {
                return null;
              }
              return Type.internalDeclared(
                  typeStructure.typeInfo(), extractMethodInfos(typeStructure), file, this);
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<MethodInfo> extractMethodInfos(TypeStructure typeStructure) {
    return typeStructure.methods().stream()
        .map(MethodStructure::methodInfo)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private void addExternalTypes(List<Type> types, List<TypeStructure> typeStructures) {
    Set<String> internalDeclaredTypeNames =
        types.stream().map(Type::getTypeInternalName).collect(Collectors.toUnmodifiableSet());
    Set<MethodCallInfo> calleeMethodCalls =
        typeStructures.stream()
            .flatMap(typeStructure -> typeStructure.methods().stream())
            .flatMap(methodStructure -> methodStructure.calleeMethods().stream())
            .filter(
                methodInfo -> !internalDeclaredTypeNames.contains(methodInfo.typeInternalName()))
            .collect(Collectors.toSet());
    Set<String> parentTypeNames =
        collectParentTypeNames(typeStructures).stream()
            .filter(parentTypeName -> !internalDeclaredTypeNames.contains(parentTypeName))
            .collect(Collectors.toSet());

    List<Type> externalTypes =
        resolveExternalTypeStructures(parentTypeNames, calleeMethodCalls).stream()
            .map(externalTypeStructure -> Type.external(externalTypeStructure, this))
            .collect(Collectors.toCollection(ArrayList::new));
    this.types.addAll(externalTypes);
  }

  private List<ExternalTypeStructure> resolveExternalTypeStructures(
      Set<String> parentTypeNames, Set<MethodCallInfo> calleeMethodCalls) {
    List<ExternalTypeStructure> parentTypes =
        ExternalTypeStructure.fromParentTypes(parentTypeNames);
    List<ExternalTypeStructure> methodCallTypes =
        ExternalTypeStructure.fromMethodCalls(calleeMethodCalls);
    Set<ExternalTypeInfo> existingTypeInfos =
        methodCallTypes.stream()
            .map(ExternalTypeStructure::externalTypeInfo)
            .collect(Collectors.toSet());
    List<ExternalTypeStructure> exceptTypes =
        parentTypes.stream()
            .filter(parent -> !existingTypeInfos.contains(parent.externalTypeInfo()))
            .toList();

    List<ExternalTypeStructure> merged = new ArrayList<>(methodCallTypes);
    merged.addAll(exceptTypes);
    return merged;
  }

  public Set<String> collectParentTypeNames(List<TypeStructure> typeStructures) {
    Set<String> result = new HashSet<>();
    for (TypeStructure ts : typeStructures) {
      TypeInfo info = ts.typeInfo();

      if (info.superName() != null) {
        result.add(info.superName());
      }
      if (info.interfaceNames() != null) {
        result.addAll(info.interfaceNames());
      }
    }

    return result;
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

  public List<OpenSourceRepoFile> getFiles() {
    return Collections.unmodifiableList(files);
  }

  public List<Type> getTypes() {
    return Collections.unmodifiableList(types);
  }
}
