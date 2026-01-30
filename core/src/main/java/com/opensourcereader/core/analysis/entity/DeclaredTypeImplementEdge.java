package com.opensourcereader.core.analysis.entity;

import com.opensourcereader.core.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeclaredTypeImplementEdge extends BaseEntity {

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "type_id")
  private DeclaredType type;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "interface_type_id")
  private DeclaredType interfaceType;

  static DeclaredTypeImplementEdge of(DeclaredType type, DeclaredType interfaceType) {
    return new DeclaredTypeImplementEdge(type, interfaceType);
  }

  private DeclaredTypeImplementEdge(DeclaredType type, DeclaredType interfaceType) {
    this.type = type;
    this.interfaceType = interfaceType;
  }
}
