package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepo extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private User owner;

  @Column private String title;

  @Column(name = "clone_url")
  private String cloneUrl;

  @OneToMany(mappedBy = "openSourceRepo", cascade = CascadeType.PERSIST)
  private final List<OpenSourceRepoContent> contents = new ArrayList<>();

  public OpenSourceRepo(String cloneUrl) {
    this.cloneUrl = cloneUrl;
  }

  public OpenSourceRepo(User owner, String title) {
    this.owner = owner;
    this.title = title;
  }

  public void addContent(OpenSourceRepoContent content) {
    contents.add(content);
  }
}
