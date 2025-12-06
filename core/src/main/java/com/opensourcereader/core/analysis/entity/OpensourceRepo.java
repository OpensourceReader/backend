package com.opensourcereader.core.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "version", nullable = false)
  private String version;

  @OneToMany(mappedBy = "opensourceRepo")
  private List<OpensourceRepoContent> opensourceRepoContents;

  public OpensourceRepo(
      String name,
      String version,
      List<OpensourceRepoContent> opensourceRepoContents
  ) {
    this.name = name;
    this.version = version;
    this.opensourceRepoContents = opensourceRepoContents;
  }

}
