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
public class TypeImplementation extends BaseEntity {

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "implemented_type_id")
  private Type implementedType;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "interface_type_id")
  private Type interfaceType;

  static TypeImplementation of(Type type, Type interfaceType) {
    return new TypeImplementation(type, interfaceType);
  }

  private TypeImplementation(Type implementedType, Type interfaceType) {
    this.implementedType = implementedType;
    this.interfaceType = interfaceType;
  }
}
