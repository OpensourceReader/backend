package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.infra.bytecode.ClassStructureExtractor;
import com.opensourcereader.core.analysis.testfixture.InMemoryJavaCompilerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class OpenSourceContentFactoryTest {

  @Autowired private OpenSourceContentFactory openSourceContentFactory;
  @Autowired private ClassStructureExtractor classStructureExtractor;

  @Test
  @DisplayName("타입(클래스)구조가 있는 파일이 아니면, 타입구조를 추가하지 않습니다.")
  void extract_Method_nonClassStructure_returnsNull() {
    // given
    String sourceFilePath = "README.md";
    List<OpenSourceFileInfo> sourceFile =
        List.of(new OpenSourceFileInfo(sourceFilePath, RepoEntryType.FILE, "anything"));
    List<ClassStructure> classStructures = List.of();
    OpenSourceRepo openSourceRepo = new OpenSourceRepo("new");

    // when
    List<OpenSourceRepoContent> contents =
        openSourceContentFactory.create(sourceFile, classStructures, openSourceRepo);

    // then
    assertThat(contents)
        .extracting(OpenSourceRepoContent::getPath, OpenSourceRepoContent::getDeclaredType)
        .containsExactlyInAnyOrder(tuple(sourceFilePath, null));
  }

  @Test
  @DisplayName("타입(클래스)구조가 있는 파일이 아니면, 타입구조를 추가합니다.")
  void createDeclareType() {
    // given
    String typeName = "com.example.ossr.A";
    String rawText =
        """
            package com.example.ossr;
            public class A {
              public void m(String s) {}
              public void n() {}
            }
            """;
    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(typeName, rawText);
    byte[] mainBytes = compiled.get(typeName);
    List<OpenSourceFileInfo> files =
        List.of(new OpenSourceFileInfo(typeName + ".java", RepoEntryType.FILE, rawText));
    List<ClassStructure> structures =
        classStructureExtractor.extract(List.of(new ClassBytecode(Path.of(typeName), mainBytes)));
    OpenSourceRepo openSourceRepo = new OpenSourceRepo("new");

    // when
    List<OpenSourceRepoContent> contents =
        openSourceContentFactory.create(files, structures, openSourceRepo);

    // then
    assertThat(contents.get(0).getDeclaredType())
        .extracting(DeclaredType::getTypeInternalName)
        .isEqualTo(typeName.replace('.', '/'));
  }

  @Test
  @DisplayName("클래스 구조(바이트코드 메서드 기준)에 소스코드 파싱을 통해 얻은 start-endLine을 기록합니다.")
  void extract_mapsSourceBySignature() {
    // given
    String className = "com.example.ossr.A";
    String rawText =
        """
            package com.example.ossr;
            public class A {
              public void m(String s) {}
              public void n() {}
            }
            """;
    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(className, rawText);
    byte[] mainBytes = compiled.get(className);
    List<OpenSourceFileInfo> files =
        List.of(new OpenSourceFileInfo(className + ".java", RepoEntryType.FILE, rawText));
    List<ClassStructure> structures =
        classStructureExtractor.extract(List.of(new ClassBytecode(Path.of(className), mainBytes)));
    OpenSourceRepo openSourceRepo = new OpenSourceRepo("new");

    // when
    List<OpenSourceRepoContent> contents =
        openSourceContentFactory.create(files, structures, openSourceRepo);

    // then
    assertThat(contents.get(0).getDeclaredType().getDeclaredMethods())
        .extracting(
            DeclaredMethod::getMethodName,
            DeclaredMethod::getMethodModifiers,
            DeclaredMethod::getParamTypes,
            DeclaredMethod::getStartLine,
            DeclaredMethod::getEndLine)
        .containsExactlyInAnyOrder(
            tuple("<init>", null, List.of(), null, null),
            tuple("m", null, List.of("java.lang.String"), 3, 3),
            tuple("n", null, List.of(), 4, 4));
  }
}
