package com.opensourcereader.core.analysis.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.domain.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.dto.MethodInfo;
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

  public static OpenSourceRepo create(
      String cloneUrl,
      List<TypeStructure> typeStructures,
      ExternalTypeStructureFactory externalTypeStructureFactory) {
    OpenSourceRepo openSourceRepo =
        new OpenSourceRepo(cloneUrl, nullSafeTypeStructures(typeStructures));
    openSourceRepo.addExternalTypes(externalTypeStructureFactory, typeStructures);
    return openSourceRepo;
  }

  private static List<TypeStructure> nullSafeTypeStructures(List<TypeStructure> typeStructures) {
    if (typeStructures == null || typeStructures.isEmpty()) {
      return new ArrayList<>();
    }
    return typeStructures;
  }

  private OpenSourceRepo(String cloneUrl, List<TypeStructure> typeStructures) {
    this.cloneUrl = cloneUrl;
    this.files = createOpenSourceRepoFiles(typeStructures);
    this.types = createTypes(this.files, typeStructures);
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
                  typeStructure.typeInfo(), MethodInfo.from(typeStructure), file, this);
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private void addExternalTypes(
      ExternalTypeStructureFactory externalTypeStructureFactory,
      List<TypeStructure> typeStructures) {
    List<Type> externalTypes =
        externalTypeStructureFactory.create(this.types, typeStructures).stream()
            .map(externalTypeStructure -> Type.external(externalTypeStructure, this))
            .collect(Collectors.toCollection(ArrayList::new));

    for (Type externalType : externalTypes) {
      if (this.types.contains(externalType)) {
        return;
      }
      this.types.add(externalType);
    }
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
