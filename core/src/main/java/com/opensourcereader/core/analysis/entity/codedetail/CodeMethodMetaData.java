package com.opensourcereader.core.analysis.entity.codedetail;

import java.util.Optional;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeMethodMetaData extends BaseEntity {

  private String methodName;

  @Enumerated(EnumType.STRING)
  private MethodModifier methodModifier;

  private Integer startLine;
  private Integer endLine;

  @ManyToOne private OpenSourceRepoContent openSourceRepoContent;

  public CodeMethodMetaData(
      String methodName,
      MethodModifier methodModifier,
      Integer startLine,
      Integer endLine,
      OpenSourceRepoContent openSourceRepoContent) {
    this.methodName = methodName;
    this.methodModifier = methodModifier;
    this.startLine = startLine;
    this.endLine = endLine;
    this.openSourceRepoContent = openSourceRepoContent;
  }

  public static CodeMethodMetaData createFrom(
      MethodDeclaration methodDeclaration, OpenSourceRepoContent openSourceRepoContent) {
    String methodName = methodDeclaration.getNameAsString();
    MethodModifier modifier = MethodModifier.from(methodDeclaration);

    // 시작 / 끝 라인 : 꺠지는 경우 대비 Optional
    Optional<Range> range = methodDeclaration.getRange();
    if (range.isPresent()) {
      int startLine = range.get().begin.line;
      int endLine = range.get().end.line;
      return new CodeMethodMetaData(
          methodName, modifier, startLine, endLine, openSourceRepoContent);
    }

    return new CodeMethodMetaData(methodName, modifier, null, null, openSourceRepoContent);
  }
}
