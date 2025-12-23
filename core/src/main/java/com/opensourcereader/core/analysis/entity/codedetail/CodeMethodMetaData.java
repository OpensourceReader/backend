package com.opensourcereader.core.analysis.entity.codedetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(
    name = "code_method",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_code_method_repo_content_method_name",
          columnNames = {"open_source_repo_content_id", "method_signature"})
    })
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeMethodMetaData extends BaseEntity {

  @Column(name = "method_name")
  private String methodName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "param_types", columnDefinition = "json", nullable = false)
  private List<String> paramTypes;

  @Enumerated(EnumType.STRING)
  private MethodModifier methodModifier;

  @Column(name = "method_signature")
  @Embedded
  private CodeMethodSignature methodSignature;

  private Integer startLine;
  private Integer endLine;

  @OneToMany(mappedBy = "caller", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private List<CodeMethodCallEdge> outgoingCalls = new ArrayList<>();

  @OneToMany(mappedBy = "callee", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private List<CodeMethodCallEdge> ingoingCalls = new ArrayList<>();

  @ManyToOne private OpenSourceRepoContent openSourceRepoContent;

  private CodeMethodMetaData(
      String methodName,
      List<String> paramTypes,
      MethodModifier methodModifier,
      CodeMethodSignature methodSignature,
      Integer startLine,
      Integer endLine,
      OpenSourceRepoContent openSourceRepoContent) {
    this.methodName = methodName;
    this.paramTypes = paramTypes;
    this.methodModifier = methodModifier;
    this.methodSignature = methodSignature;
    this.startLine = startLine;
    this.endLine = endLine;
    this.openSourceRepoContent = openSourceRepoContent;
  }

  public static CodeMethodMetaData of(
      MethodDeclaration methodDeclaration, OpenSourceRepoContent openSourceRepoContent) {
    String methodName = methodDeclaration.getNameAsString();
    MethodModifier modifier = MethodModifier.from(methodDeclaration);
    List<String> paramTypes = getParameterTypes(methodDeclaration);

    return new CodeMethodMetaData(
        methodName,
        paramTypes,
        modifier,
        CodeMethodSignature.of(methodName, paramTypes), // 바이트 코드 추출떄랑 통일 필요
        getStartLine(methodDeclaration),
        getEndLine(methodDeclaration),
        openSourceRepoContent);
  }

  private static List<String> getParameterTypes(MethodDeclaration methodDeclaration) {
    List<String> paramTypes = new ArrayList<>();
    for (Parameter methodParameter : methodDeclaration.getParameters()) {
      paramTypes.add(methodParameter.getType().toString());
    }
    return paramTypes;
  }

  private static Integer getStartLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().begin.line;
  }

  private static Integer getEndLine(MethodDeclaration methodDeclaration) {
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isEmpty()) {
      return null;
    }
    return range.get().end.line;
  }

  public void updateAllOutgoingCalls(List<CodeMethodCallEdge> methodCallEdges) {
    this.outgoingCalls = methodCallEdges;
  }

  public void updateAllIngoingCalls(List<CodeMethodCallEdge> methodCallEdges) {
    this.ingoingCalls = methodCallEdges;
  }

  public void updateIngoingCall(CodeMethodCallEdge methodCallEdge) {
    Set<Long> existingCallerIds =
        ingoingCalls.stream().map(BaseEntity::getId).collect(Collectors.toSet());
    if (existingCallerIds.contains(methodCallEdge.getId())) {
      return;
    }
    this.ingoingCalls.add(methodCallEdge);
  }
}
