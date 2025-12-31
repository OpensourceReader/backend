package com.opensourcereader.core.analysis.entity.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
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
public class CodeMethod extends BaseEntity {

  @Column(name = "class_internal_name")
  private String classInternalName;

  @Column(name = "method_name")
  private String methodName;

  @Column(name = "return_type")
  private String returnType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "param_types", columnDefinition = "json", nullable = false)
  private List<String> paramTypes;

  @Enumerated(EnumType.STRING)
  @Column(name = "method_modifier")
  private AccessModifier accessModifier;

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
  private List<CodeMethodCallEdge> outgoingCalls = new ArrayList<>();

  @OneToMany(
      mappedBy = "callee",
      fetch = FetchType.LAZY,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  private List<CodeMethodCallEdge> ingoingCalls = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "open_source_repo_content_id")
  private OpenSourceRepoContent openSourceRepoContent;

  public static CodeMethod internal(
      CodeMethodExtractResult methodExtractResult, OpenSourceRepoContent openSourceRepoContent) {
    return new CodeMethod(
        openSourceRepoContent.getClassInternalName(),
        methodExtractResult.methodName(),
        methodExtractResult.returnType(),
        methodExtractResult.paramTypes(),
        methodExtractResult.modifier(),
        CodeMethodSignature.of(methodExtractResult),
        methodExtractResult.startLine(),
        methodExtractResult.endLine(),
        MethodOrigin.INTERNAL,
        openSourceRepoContent);
  }

  public static CodeMethod external(MethodCallInfo callee, CodeMethodSignature methodSignature) {
    return new CodeMethod(
        callee.className(),
        callee.methodName(),
        callee.descriptor().methodReturnType(),
        callee.descriptor().argumentTypes(),
        null,
        methodSignature,
        null,
        null,
        MethodOrigin.EXTERNAL,
        null);
  }

  public static CodeMethod external(String interfaceName, CodeMethod caller) {
    return new CodeMethod(
        interfaceName,
        caller.methodName,
        caller.returnType,
        caller.paramTypes,
        caller.accessModifier,
        caller.methodSignature,
        caller.startLine,
        caller.endLine,
        MethodOrigin.EXTERNAL,
        null);
  }

  private CodeMethod(
      String classInternalName,
      String methodName,
      String returnType,
      List<String> paramTypes,
      AccessModifier accessModifier,
      CodeMethodSignature methodSignature,
      Integer startLine,
      Integer endLine,
      MethodOrigin methodOrigin,
      OpenSourceRepoContent openSourceRepoContent) {
    this.classInternalName = classInternalName;
    this.methodName = methodName;
    this.returnType = returnType;
    this.paramTypes = paramTypes;
    this.accessModifier = accessModifier;
    this.methodSignature = methodSignature;
    this.startLine = startLine;
    this.endLine = endLine;
    this.origin = methodOrigin;
    this.openSourceRepoContent = openSourceRepoContent;
  }

  public void updateAllCalls(
      List<CodeMethodCallEdge> newOutgoingCalls, List<CodeMethodCallEdge> newIngoingCalls) {
    syncEdges(this.outgoingCalls, newOutgoingCalls);
    syncEdges(this.ingoingCalls, newIngoingCalls);
  }

  private static void syncEdges(
      List<CodeMethodCallEdge> current, List<CodeMethodCallEdge> incoming) {
    current.removeIf(edge -> !incoming.contains(edge));

    for (CodeMethodCallEdge edge : incoming) {
      if (!current.contains(edge)) {
        current.add(edge);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CodeMethod that)) {
      return false;
    }
    return Objects.equals(methodSignature, that.methodSignature)
        && Objects.equals(openSourceRepoContent, that.openSourceRepoContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(methodSignature, openSourceRepoContent);
  }
}
