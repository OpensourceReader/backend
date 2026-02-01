package com.opensourcereader.core.analysis.entity;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.entity.method.MethodSignature;
import com.opensourcereader.core.shared.BaseEntity;
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
public class Method extends BaseEntity {

  @Column(name = "type_internal_name")
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
  private List<MethodModifier> methodModifiers;

  @Column(name = "method_signature")
  @Embedded
  private MethodSignature methodSignature;

  @Column(name = "start_line")
  private Integer startLine;

  @Column(name = "end_line")
  private Integer endLine;

  @Enumerated(EnumType.STRING)
  @Column(name = "method_origin")
  private MethodOrigin origin;

  @OneToMany(
      mappedBy = "caller",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  private final List<MethodCallEdge> outgoingCalls = new ArrayList<>();

  @OneToMany(
      mappedBy = "callee",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  private final List<MethodCallEdge> ingoingCalls = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "type_id")
  private Type type;

  static Method declared(MethodInfo methodInfo, Type type) {
    return new Method(
        type.getTypeInternalName(),
        methodInfo.methodName(),
        methodInfo.methodDescriptor().methodReturnType(),
        methodInfo.methodDescriptor().argumentTypes(),
        methodInfo.methodModifiers().stream().toList(),
        MethodSignature.of(methodInfo),
        methodInfo.startLine(),
        methodInfo.endLine(),
        MethodOrigin.DECLARED,
        type);
  }

  static Method inheritedInternal(Method interfaceMethod, Type implType) {
    return new Method(
        implType.getTypeInternalName(),
        interfaceMethod.methodName,
        interfaceMethod.returnType,
        interfaceMethod.paramTypes,
        interfaceMethod.methodModifiers,
        interfaceMethod.getMethodSignature(),
        null,
        null,
        MethodOrigin.INHERITED_INTERNAL,
        implType);
  }

  static Method inheritedExternal(Method superMethod, Type childType) {
    return new Method(
        childType.getTypeInternalName(),
        superMethod.methodName,
        superMethod.returnType,
        superMethod.paramTypes,
        superMethod.methodModifiers,
        superMethod.getMethodSignature(),
        null,
        null,
        MethodOrigin.INHERITED_EXTERNAL,
        childType);
  }

  public static Method external(MethodCallInfo calleeMethodInfo) {
    return new Method(
        calleeMethodInfo.typeInternalName(),
        calleeMethodInfo.methodName(),
        calleeMethodInfo.descriptor().methodReturnType(),
        calleeMethodInfo.descriptor().argumentTypes(),
        null,
        MethodSignature.of(calleeMethodInfo),
        null,
        null,
        MethodOrigin.EXTERNAL,
        null);
  }

  private Method(
      String typeInternalName,
      String methodName,
      String returnType,
      List<String> paramTypes,
      List<MethodModifier> methodModifiers,
      MethodSignature methodSignature,
      Integer startLine,
      Integer endLine,
      MethodOrigin methodOrigin,
      Type type) {
    this.typeInternalName = typeInternalName;
    this.methodName = methodName;
    this.returnType = returnType;
    this.paramTypes = paramTypes;
    this.methodModifiers = methodModifiers;
    this.methodSignature = methodSignature;
    this.startLine = startLine;
    this.endLine = endLine;
    this.origin = methodOrigin;
    this.type = type;
  }

  public void addIngoingCall(Method caller) {
    if (caller == null) {
      return;
    }
    MethodCallEdge ingoingCall = MethodCallEdge.of(caller, this);
    if (this.ingoingCalls.contains(ingoingCall)) {
      return;
    }
    this.ingoingCalls.add(ingoingCall);

    caller.syncOutgoingCall(ingoingCall);
  }

  public void addOutgoingCall(Method callee) {
    if (callee == null) {
      return;
    }
    MethodCallEdge outgoingCall = MethodCallEdge.of(this, callee);
    if (this.outgoingCalls.contains(outgoingCall)) {
      return;
    }
    this.outgoingCalls.add(outgoingCall);

    callee.syncIngoingCall(outgoingCall);
  }

  private void syncIngoingCall(MethodCallEdge targetOutgoingCall) {
    if (targetOutgoingCall == null) {
      return;
    }
    if (this.ingoingCalls.contains(targetOutgoingCall)) {
      return;
    }
    this.ingoingCalls.add(targetOutgoingCall);
  }

  private void syncOutgoingCall(MethodCallEdge targetIngoingCall) {
    if (targetIngoingCall == null) {
      return;
    }
    if (this.outgoingCalls.contains(targetIngoingCall)) {
      return;
    }
    this.outgoingCalls.add(targetIngoingCall);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Method that)) {
      return false;
    }
    return Objects.equals(typeInternalName, that.typeInternalName)
        && Objects.equals(methodSignature, that.methodSignature)
        && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeInternalName, methodSignature, type);
  }
}
