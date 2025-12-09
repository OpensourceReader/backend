package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "clone_url")
  private String cloneUrl;

  @OneToMany(mappedBy = "openSourceRepo", cascade = CascadeType.PERSIST)
  private final List<OpenSourceRepoContent> contents = new ArrayList<>();

  public OpenSourceRepo(String cloneUrl) {
    this.cloneUrl = cloneUrl;
  }

  public void addContent(OpenSourceRepoContent content) {
    contents.add(content);
  }
}
