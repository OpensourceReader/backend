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
public class MethodCallEdge extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "method_call_origin")
  private MethodCallOrigin methodCallOrigin;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "caller_id")
  private Method caller;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "callee_id")
  private Method callee;

  private MethodCallEdge(Method caller, Method callee, MethodCallOrigin methodCallOrigin) {
    this.caller = caller;
    this.callee = callee;
    this.methodCallOrigin = methodCallOrigin;
  }

  static MethodCallEdge of(Method caller, Method callee) {
    return new MethodCallEdge(caller, callee, getCodeMethodCallEdge(caller, callee));
  }

  private static MethodCallOrigin getCodeMethodCallEdge(Method caller, Method callee) {
    if (caller.getOrigin().equals(MethodOrigin.EXTERNAL)
        || callee.getOrigin().equals(MethodOrigin.EXTERNAL)) {
      return MethodCallOrigin.EXTERNAL;
    }
    return MethodCallOrigin.INTERNAL;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MethodCallEdge that)) {
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
