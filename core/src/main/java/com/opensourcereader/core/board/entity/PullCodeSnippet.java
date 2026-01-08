package com.opensourcereader.core.board.entity;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pull_code_snippet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullCodeSnippet extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_request_file_id")
  private PullRequestFile pullRequestFile;

  @Column private Integer startLine;

  @Column private Integer endLine;

  private PullCodeSnippet(PullRequestFile pullRequestFile, Integer startLine, Integer endLine) {
    super();
    this.pullRequestFile = pullRequestFile;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  public static PullCodeSnippet of(
      PullRequestFile pullRequestFile, Integer startLine, Integer endLine) {
    return new PullCodeSnippet(pullRequestFile, startLine, endLine);
  }
}
