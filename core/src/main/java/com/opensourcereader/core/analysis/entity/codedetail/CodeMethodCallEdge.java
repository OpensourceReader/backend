package com.opensourcereader.core.analysis.entity.codedetail;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeMethodCallEdge extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "caller_id")
  private CodeMethod caller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "callee_id")
  private CodeMethod callee;

  public CodeMethodCallEdge(CodeMethod caller, CodeMethod callee) {
    this.caller = caller;
    this.callee = callee;
  }
}
