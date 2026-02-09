package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;
import com.opensourcereader.core.analysis.domain.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.infra.bytecode.ClassStructureExtractor;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeMethodStructure;
import com.opensourcereader.core.analysis.infra.dto.ClassBytecode;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;
import com.opensourcereader.core.analysis.infra.parser.SourceFileParser;
import com.opensourcereader.core.analysis.service.impl.repoartifact.TypeStructureResolver;
import com.opensourcereader.core.analysis.testfixture.InMemoryJavaCompilerFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class TypeStructureResolverTest {

  @Autowired private TypeStructureResolver resolver;
  @Autowired private SourceFileParser sourceFileParser;
  @Autowired private ClassStructureExtractor classStructureExtractor;

  @Disabled
  @Test
  @DisplayName("매개변수 외부타입테스트")
  void astParserAndByteCodeParserCreateSameSignature_OtherArgument() {

    // given
    String repoSource =
        """
            package sample;

            public class Repo {
                private final String name;

                public Repo(String name) {
                    this.name = name;
                }
            }
        """;

    String serviceSource =
        """
            package sample;

            public class SampleService {

                public Repo createRepo(Repo repo, String name, int version) {
                    return new Repo(name + version);
                }
            }
        """;

    Map<String, String> sources =
        Map.of(
            "sample/Repo.java", repoSource,
            "sample/SampleService.java", serviceSource);

    // when

    // 1️⃣ 소스 기반 시그니처 추출
    ParsedSourceFile parsed =
        sourceFileParser.parse(
            new OpenSourceFileInfo("sample/SampleService.java", RepoFileType.FILE, serviceSource));

    Set<MethodSignature> parsedSignatures =
        parsed.methods().values().stream().map(MethodSignature::from).collect(Collectors.toSet());

    // 2️⃣ 바이트코드 기반 시그니처 추출
    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);

    ByteCodeClassStructure byteCodeClassStructure =
        classStructureExtractor
            .extract(
                List.of(
                    new ClassBytecode(
                        Path.of("sample/SampleService.class"),
                        compiled.get("sample.SampleService"))))
            .get(0);

    Set<MethodSignature> bytecodeSignatures =
        byteCodeClassStructure.methods().stream()
            .map(ByteCodeMethodStructure::byteCodeDeclaredMethodInfo)
            .map(MethodSignature::from)
            .collect(Collectors.toSet());

    // then
    assertThat(bytecodeSignatures).containsExactlyInAnyOrderElementsOf(parsedSignatures);
  }

  @Disabled
  @Test
  @DisplayName("리턴타입테스트")
  void astParserAndByteCodeParserCreateSameSignature_twoClass() {
    // given
    String repoSource =
        """
            package sample;

            public class Repo {
                private final String name;

                public Repo(String name) {
                    this.name = name;
                }
            }
        """;
    String serviceSource =
        """
            package sample;

            public class SampleService {

                public Repo createRepo(String name) {
                    return new Repo(name);
                }
            }
        """;
    Map<String, String> sources =
        Map.of(
            "sample/Repo.java", repoSource,
            "sample/SampleService.java", serviceSource);

    // when
    ParsedSourceFile parse =
        sourceFileParser.parse(
            new OpenSourceFileInfo("sample.SampleService.java", RepoFileType.FILE, serviceSource));
    List<MethodSignature> parsedSourceFileSignature =
        parse.methods().entrySet().stream()
            .map(Entry::getValue)
            .map(MethodSignature::from)
            .toList();

    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
    ByteCodeClassStructure byteCodeClassStructure =
        classStructureExtractor
            .extract(
                List.of(
                    new ClassBytecode(
                        Path.of("sample.SampleService.java"),
                        compiled.get("sample.SampleService"))))
            .get(0);
    List<MethodSignature> byteCodeMethodSignature =
        byteCodeClassStructure.methods().stream()
            .map(ByteCodeMethodStructure::byteCodeDeclaredMethodInfo)
            .map(MethodSignature::from)
            .toList();

    // then
    assertThat(byteCodeClassStructure).isNotNull();
  }

  @Disabled
  @Test
  @DisplayName("리스트일떄 파서랑, 바이트 코드랑 반환값이 다름;;")
  void astParserAndByteCodeParserCreateSameSignature() {
    // given
    String rawText =
        """
          package sample;

          import java.util.List;

          public class SampleService {

              private final String name;

              public SampleService(String name) {
                  this.name = name;
              }

              public List<String> createRepo(String owner, String repo, int version) {
                  return List.of(owner + "/" + repo + ":" + version);
              }

              public static int sum(int a, int b) {
                  return a + b;
              }
          }
        """;
    String fqcn = "sample.SampleService";

    // when
    ParsedSourceFile parse =
        sourceFileParser.parse(new OpenSourceFileInfo(fqcn + ".java", RepoFileType.FILE, rawText));
    List<MethodSignature> parsedSourceFileSignature =
        parse.methods().entrySet().stream()
            .map(Entry::getValue)
            .map(MethodSignature::from)
            .toList();
    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(fqcn, rawText);
    ByteCodeClassStructure byteCodeClassStructure =
        classStructureExtractor
            .extract(List.of(new ClassBytecode(Path.of(fqcn + ".java"), compiled.get(fqcn))))
            .get(0);
    List<MethodSignature> byteCodeMethodSignature =
        byteCodeClassStructure.methods().stream()
            .map(ByteCodeMethodStructure::byteCodeDeclaredMethodInfo)
            .map(MethodSignature::from)
            .toList();

    // then
    assertThat(byteCodeClassStructure).isNotNull();
    assertThat(byteCodeClassStructure.methods().get(0)).isNotNull();
  }

  @Test
  @DisplayName("parsedFile이 null이면 TypeStructure는 생성하되 typeInfo/methods는 null로 둔다")
  void resolve_returnsNulls_whenParsedFileIsNull() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(), RepoFileType.FILE, "class A {}");

    // when
    TypeStructure result = resolver.resolve(sourceFile, null, Map.of());

    // then
    assertThat(result).isNotNull();
    assertThat(result.typeInfo()).isNull();
    assertThat(result.methods()).isEmpty();
  }

  @Test
  @DisplayName("parsedFile과 매칭되는 bytecode가 없으면 빈 TypeStructure를 반환한다")
  void resolve_returnsEmptyTypeStructure_whenBytecodeNotFound() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(), RepoFileType.FILE, "class A {}");

    ParsedSourceFile parsedFile = new ParsedSourceFile("com/example/A", Map.of());

    // when
    TypeStructure result = resolver.resolve(sourceFile, parsedFile, Map.of());

    // then
    assertThat(result).isNotNull();
    assertThat(result.typeInfo()).isNull();
    assertThat(result.methods()).isEmpty();
  }

  @Test
  @DisplayName("parsedFile 결과와 바이트코드 결과가 매칭되면 TypeStructure와 내부의 MethodStructure도 생성된다")
  void resolve_createsTypeStructure_andMethodStructures_whenMatched() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(),
            RepoFileType.FILE,
            "class A { String hello(){} }");

    String typeInternalName = "com/example/A";
    String methodInternalName = "hello";
    SourceCodeParseResult parseResult =
        new SourceCodeParseResult(
            methodInternalName,
            EnumSet.of(MethodModifier.PUBLIC),
            "java.lang.String",
            List.of("int"),
            10,
            12);
    ParsedSourceFile parsedFile =
        new ParsedSourceFile(
            typeInternalName, Map.of(MethodSignature.from(parseResult), parseResult));
    Map<String, ByteCodeClassStructure> bytecodes =
        getByteCodeClassStructure(typeInternalName, methodInternalName);

    // when
    TypeStructure result = resolver.resolve(sourceFile, parsedFile, bytecodes);

    // then
    MethodStructure methodStructure = result.methods().get(0);
    assertThat(methodStructure.methodInfo().startLine()).isEqualTo(10);
    assertThat(methodStructure.methodInfo().endLine()).isEqualTo(12);
    assertThat(methodStructure.methodInfo().methodName()).isEqualTo(methodInternalName);
  }

  private static Map<String, ByteCodeClassStructure> getByteCodeClassStructure(
      String typeInternalName, String methodInternalName) {
    MethodDescriptor descriptor = MethodDescriptor.from("(I)Ljava/lang/String;");
    ByteCodeDeclaredMethodInfo declaredMethodInfo =
        new ByteCodeDeclaredMethodInfo(
            typeInternalName,
            methodInternalName,
            EnumSet.of(MethodModifier.PUBLIC),
            descriptor,
            null,
            List.of());
    TypeInfo typeInfo = new TypeInfo(183, TypeKind.CLASS, typeInternalName, null, null, null);
    ByteCodeClassStructure byteCodeClassStructure =
        new ByteCodeClassStructure(
            typeInfo, List.of(new ByteCodeMethodStructure(declaredMethodInfo, List.of())));
    return Map.of(typeInternalName, byteCodeClassStructure);
  }
}
