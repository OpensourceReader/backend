package com.opensourcereader.core.analysis.entity.codedetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
public class CodeMethodMetaData extends BaseEntity {

  private String methodName;

  private String parameterSignature;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "param_types", columnDefinition = "json", nullable = false)
  private List<String> paramTypes;

  @Enumerated(EnumType.STRING)
  private MethodModifier methodModifier;

  private Integer startLine;
  private Integer endLine;

  @OneToMany(mappedBy = "caller", fetch = FetchType.LAZY)
  private List<CodeMethodCallEdge> outgoingCalls;

  @OneToMany(mappedBy = "callee", fetch = FetchType.LAZY)
  private List<CodeMethodCallEdge> ingoingCalls;

  @ManyToOne private OpenSourceRepoContent openSourceRepoContent;

  private CodeMethodMetaData(
      String methodName,
      String parameterSignature,
      List<String> paramTypes,
      MethodModifier methodModifier,
      Integer startLine,
      Integer endLine,
      OpenSourceRepoContent openSourceRepoContent) {
    this.methodName = methodName;
    this.parameterSignature = parameterSignature;
    this.paramTypes = paramTypes;
    this.methodModifier = methodModifier;
    this.startLine = startLine;
    this.endLine = endLine;
    this.openSourceRepoContent = openSourceRepoContent;
  }

  public static CodeMethodMetaData of(
      MethodDeclaration methodDeclaration, OpenSourceRepoContent openSourceRepoContent) {
    String methodName = methodDeclaration.getNameAsString();
    MethodModifier modifier = MethodModifier.from(methodDeclaration);

    List<String> paramTypes = getParameterTypes(methodDeclaration);
    String parameterTypeSignature = String.join("", paramTypes);
    Integer startLine = getStartLine(methodDeclaration);
    Integer endLine = getEndLine(methodDeclaration);
    return new CodeMethodMetaData(
        methodName,
        parameterTypeSignature,
        paramTypes,
        modifier,
        startLine,
        endLine,
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

  public void updateOutgoingCalls(List<CodeMethodCallEdge> methodCallEdges) {
    this.outgoingCalls = methodCallEdges;
  }
}
