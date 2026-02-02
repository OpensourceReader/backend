package com.opensourcereader.core.analysis.domain.entity;

import java.util.Objects;

import com.opensourcereader.core.shared.BaseEntity;
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
public class MethodCallEdge extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "caller_id")
  private Method caller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "callee_id")
  private Method callee;

  private MethodCallEdge(Method caller, Method callee) {
    this.caller = caller;
    this.callee = callee;
  }

  static MethodCallEdge of(Method caller, Method callee) {
    return new MethodCallEdge(caller, callee);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MethodCallEdge that)) {
      return false;
    }
    return Objects.equals(caller, that.caller) && Objects.equals(callee, that.callee);
  }

  @Override
  public int hashCode() {
    return Objects.hash(caller, callee);
  }
}
