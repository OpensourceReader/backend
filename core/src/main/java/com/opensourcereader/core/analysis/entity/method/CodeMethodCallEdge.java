package com.opensourcereader.core.analysis.entity.method;

import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "method_call_origin")
  private MethodCallOrigin methodCallOrigin;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "caller_id")
  private DeclaredMethod caller;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "callee_id")
  private DeclaredMethod callee;

  private CodeMethodCallEdge(
      DeclaredMethod caller, DeclaredMethod callee, MethodCallOrigin methodCallOrigin) {
    this.caller = caller;
    this.callee = callee;
    this.methodCallOrigin = methodCallOrigin;
  }

  public static CodeMethodCallEdge of(DeclaredMethod caller, DeclaredMethod callee) {
    return new CodeMethodCallEdge(caller, callee, getCodeMethodCallEdge(caller, callee));
  }

  private static MethodCallOrigin getCodeMethodCallEdge(
      DeclaredMethod caller, DeclaredMethod callee) {
    if (caller.getOrigin().equals(MethodOrigin.EXTERNAL_RESOLVED)
        || callee.getOrigin().equals(MethodOrigin.EXTERNAL_RESOLVED)) {
      return MethodCallOrigin.EXTERNAL;
    }
    return MethodCallOrigin.INTERNAL;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CodeMethodCallEdge that)) {
      return false;
    }
    return methodCallOrigin == that.methodCallOrigin
        && Objects.equals(caller, that.caller)
        && Objects.equals(callee, that.callee);
  }

  @Override
  public int hashCode() {
    return Objects.hash(methodCallOrigin, caller, callee);
  }
}
