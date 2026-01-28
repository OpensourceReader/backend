package com.opensourcereader.core.analysis.entity.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.TypeStructure;
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

  @OneToMany(mappedBy = "openSourceRepo", cascade = CascadeType.PERSIST)
  private List<OpenSourceRepoContent> contents = new ArrayList<>();

  public static OpenSourceRepo of(String cloneUrl, List<TypeStructure> typeStructures) {
    return new OpenSourceRepo(cloneUrl, typeStructures);
  }

  private OpenSourceRepo(String cloneUrl, List<TypeStructure> typeStructures) {
    this.cloneUrl = cloneUrl;
    this.contents =
        typeStructures.stream()
            .map(structure -> OpenSourceRepoContent.of(structure, this))
            .collect(Collectors.toCollection(ArrayList::new));
  }

  public void addAllContents(List<OpenSourceRepoContent> contents) {
    this.contents = contents;
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
