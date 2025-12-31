package com.opensourcereader.core.analysis.entity.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
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

  public OpenSourceRepo(String cloneUrl) {
    this.cloneUrl = cloneUrl;
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
