package com.opensourcereader.core.analysis.entity.codemethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
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

  @Column(name = "method_name")
  private String methodName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "param_types", columnDefinition = "json", nullable = false)
  private List<String> paramTypes;

  @Enumerated(EnumType.STRING)
  private AccessModifier accessModifier;

  @Column(name = "method_signature")
  @Embedded
  private CodeMethodSignature methodSignature;

  private Integer startLine;
  private Integer endLine;

  @OneToMany(mappedBy = "caller", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private List<CodeMethodCallEdge> outgoingCalls = new ArrayList<>();

  @OneToMany(mappedBy = "callee", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private List<CodeMethodCallEdge> ingoingCalls = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "open_source_repo_content_id", nullable = false)
  private OpenSourceRepoContent openSourceRepoContent;

  public static CodeMethod of(
      CodeMethodExtractResult methodExtractResult, OpenSourceRepoContent openSourceRepoContent) {
    return new CodeMethod(
        methodExtractResult.methodName(),
        methodExtractResult.paramTypes(),
        methodExtractResult.modifier(),
        CodeMethodSignature.of(methodExtractResult.methodName(), methodExtractResult.paramTypes()),
        methodExtractResult.startLine(),
        methodExtractResult.endLine(),
        openSourceRepoContent);
  }

  private CodeMethod(
      String methodName,
      List<String> paramTypes,
      AccessModifier accessModifier,
      CodeMethodSignature methodSignature,
      Integer startLine,
      Integer endLine,
      OpenSourceRepoContent openSourceRepoContent) {
    this.methodName = methodName;
    this.paramTypes = paramTypes;
    this.accessModifier = accessModifier;
    this.methodSignature = methodSignature;
    this.startLine = startLine;
    this.endLine = endLine;
    this.openSourceRepoContent = openSourceRepoContent;
  }

  public void updateAllOutgoingCalls(List<CodeMethodCallEdge> methodCallEdges) {
    this.outgoingCalls.removeIf(edge -> !methodCallEdges.contains(edge));

    for (CodeMethodCallEdge edge : methodCallEdges) {
      if (this.outgoingCalls.contains(edge)) {
        continue;
      }
      this.outgoingCalls.add(edge);
    }
  }

  public void updateAllIngoingCalls(List<CodeMethodCallEdge> methodCallEdges) {
    this.ingoingCalls.removeIf(edge -> !methodCallEdges.contains(edge));

    for (CodeMethodCallEdge edge : methodCallEdges) {
      if (this.ingoingCalls.contains(edge)) {
        continue;
      }
      this.ingoingCalls.add(edge);
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
