package com.opensourcereader.core.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpensourceRepo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String cloneUrl;

  @OneToMany(mappedBy = "opensourceRepo")
  @Column(name = "contents")
  private List<OpensourceRepoContent> contents = new ArrayList<>();

  public OpensourceRepo(String cloneUrl) {
    this.cloneUrl = cloneUrl;
  }

  public void addContent(OpensourceRepoContent content) {
    contents.add(content);
  }

}
