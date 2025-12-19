package com.opensourcereader.core.analysis.entity;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.opensourcereader.core.BaseEntity;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenSourceRepoContent extends BaseEntity {

  @Column(name = "path", nullable = false)
  private String path;

  @Column(name = "name", nullable = false)
  @Embedded
  private OpenSourceRepoContentName name;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  // enum 수정필요
  @Column(name = "extension")
  private String extension;

  @Lob
  @Column(name = "raw_text", columnDefinition = "LONGTEXT")
  private String rawText;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "opensource_repo_content_id")
  private List<CodeMethodMetaData> codeMethodMetaData;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opensource_repository_id", nullable = false)
  private OpenSourceRepo openSourceRepo;

  public static OpenSourceRepoContent of(
      GitTreeFileInfo fileInfo, String rawText, OpenSourceRepo openSourceRepo) {
    return new OpenSourceRepoContent(fileInfo.path(), fileInfo.type(), rawText, openSourceRepo);
  }

  private OpenSourceRepoContent(
      String path, ContentType contentType, String rawText, OpenSourceRepo openSourceRepo) {
    this.path = path;
    this.extension = OpenSourceRepoContentName.getExtension(path);
    this.name = OpenSourceRepoContentName.from(path);
    this.contentType = contentType;
    this.rawText = rawText;
    this.codeMethodMetaData = separateCodeMethods(rawText);
    this.openSourceRepo = openSourceRepo;
  }

  private List<CodeMethodMetaData> separateCodeMethods(String rawText) {
    if (this.name.isNotJavaFile()) {
      return new ArrayList<>();
    }
    List<CodeMethodMetaData> result = new ArrayList<>();
    StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    CompilationUnit compilationUnit = StaticJavaParser.parse(rawText);
    for (MethodDeclaration methodDeclaration : compilationUnit.findAll(MethodDeclaration.class)) {
      CodeMethodMetaData methodMetaData = CodeMethodMetaData.of(methodDeclaration, this);
      result.add(methodMetaData);
    }
    return result;
  }
}
