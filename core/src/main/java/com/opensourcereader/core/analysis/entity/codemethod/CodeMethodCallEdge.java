package com.opensourcereader.core.analysis.entity.codemethod;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.CascadeType;
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

  private MethodCallOrigin methodCallOrigin;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "caller_id")
  private CodeMethod caller;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "callee_id")
  private CodeMethod callee;

  private CodeMethodCallEdge(
      CodeMethod caller, CodeMethod callee, MethodCallOrigin methodCallOrigin) {
    this.caller = caller;
    this.callee = callee;
    this.methodCallOrigin = methodCallOrigin;
  }

  public static CodeMethodCallEdge of(CodeMethod caller, CodeMethod callee) {
    return new CodeMethodCallEdge(caller, callee, getCodeMethodCallEdge(caller, callee));
  }

  private static MethodCallOrigin getCodeMethodCallEdge(CodeMethod caller, CodeMethod callee) {
    if (caller.getOrigin().equals(MethodOrigin.EXTERNAL)
        || callee.getOrigin().equals(MethodOrigin.EXTERNAL)) {
      return MethodCallOrigin.EXTERNAL;
    }
    return MethodCallOrigin.INTERNAL;
  }
}
