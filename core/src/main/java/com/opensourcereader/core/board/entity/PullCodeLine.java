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
@Table(name = "pull_code_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullCodeLine extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pull_code_id")
  private PullCode pullCode;

  @Column
  private Integer startLine;

  @Column
  private Integer endLine;

  private PullCodeLine(PullCode pullCode, Integer startLine, Integer endLine) {
    super();
    this.pullCode = pullCode;
    this.startLine = startLine;
    this.endLine = endLine;
  }

  public static PullCodeLine of(PullCode pullCode, Integer startLine, Integer endLine) {
    return new PullCodeLine(pullCode, startLine, endLine);
  }
}
