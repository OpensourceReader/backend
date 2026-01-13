package com.opensourcereader.core.analysis.entity.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeclaredMethod extends BaseEntity {

  @Column(name = "class_internal_name")
  private String typeInternalName;

  @Column(name = "method_name")
  private String methodName;

  @Column(name = "return_type")
  private String returnType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "param_types", columnDefinition = "json", nullable = false)
  private List<String> paramTypes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "method_modifier")
  private List<MethodModifier> methodModifiers; // 수정필요

  @Column(name = "method_signature")
  @Embedded
  private CodeMethodSignature methodSignature;

  @Column(name = "start_line")
  private Integer startLine;

  @Column(name = "end_line")
  private Integer endLine;

  @Enumerated(EnumType.STRING)
  @Column(name = "origin")
  private MethodOrigin origin;

  @OneToMany(
      mappedBy = "caller",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  private final List<CodeMethodCallEdge> outgoingCalls = new ArrayList<>();

  @OneToMany(
      mappedBy = "callee",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  private final List<CodeMethodCallEdge> ingoingCalls = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "declared_type_id")
  private DeclaredType declaredType;

  public static DeclaredMethod internalInheritanceDeclared(
      MethodCallInfo callee, CodeMethodSignature methodSignature) {
    return new DeclaredMethod(
        callee.className(),
        callee.methodName(),
        callee.descriptor().methodReturnType(),
        callee.descriptor().argumentTypes(),
        null,
        methodSignature,
        null,
        null,
        MethodOrigin.INTERNAL_INHERITED_RESOLVED,
        null);
  }

  public static DeclaredMethod internalInheritanceDeclared(
      DeclaredMethod superDeclaredMethod, DeclaredType childDeclaredType) {
    return new DeclaredMethod(
        childDeclaredType.getTypeInternalName(),
        superDeclaredMethod.methodName,
        superDeclaredMethod.returnType,
        superDeclaredMethod.paramTypes,
        superDeclaredMethod.methodModifiers,
        superDeclaredMethod.getMethodSignature(),
        null,
        null,
        MethodOrigin.INTERNAL_INHERITED_DECLARATION,
        childDeclaredType);
  }

  public static DeclaredMethod internal(
      CodeMethodExtractResult methodExtractResult, DeclaredType declaredType) {
    return new DeclaredMethod(
        declaredType.getTypeInternalName(),
        methodExtractResult.methodName(),
        methodExtractResult.returnType(),
        methodExtractResult.paramTypes(),
        methodExtractResult.methodModifiers().stream().toList(),
        CodeMethodSignature.of(methodExtractResult),
        methodExtractResult.startLine(),
        methodExtractResult.endLine(),
        MethodOrigin.INTERNAL_DECLARED,
        declaredType);
  }

  public static DeclaredMethod external(
      MethodCallInfo callee, CodeMethodSignature methodSignature) {
    return new DeclaredMethod(
        callee.className(),
        callee.methodName(),
        callee.descriptor().methodReturnType(),
        callee.descriptor().argumentTypes(),
        null,
        methodSignature,
        null,
        null,
        MethodOrigin.EXTERNAL_RESOLVED,
        null);
  }

  private DeclaredMethod(
      String typeInternalName,
      String methodName,
      String returnType,
      List<String> paramTypes,
      List<MethodModifier> methodModifiers,
      CodeMethodSignature methodSignature,
      Integer startLine,
      Integer endLine,
      MethodOrigin methodOrigin,
      DeclaredType declaredType) {
    this.typeInternalName = typeInternalName;
    this.methodName = methodName;
    this.returnType = returnType;
    this.paramTypes = paramTypes;
    this.methodModifiers = methodModifiers;
    this.methodSignature = methodSignature;
    this.startLine = startLine;
    this.endLine = endLine;
    this.origin = methodOrigin;
    this.declaredType = declaredType;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DeclaredMethod that)) {
      return false;
    }
    return Objects.equals(typeInternalName, that.typeInternalName)
        && Objects.equals(methodSignature, that.methodSignature)
        && Objects.equals(declaredType, that.declaredType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeInternalName, methodSignature, declaredType);
  }
}
