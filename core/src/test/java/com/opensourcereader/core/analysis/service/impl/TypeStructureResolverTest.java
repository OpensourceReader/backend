package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeMethodStructure;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TypeStructureResolverTest {

  private final TypeStructureResolver resolver = new TypeStructureResolver();

  @Test
  @DisplayName("parsedFile이 null이면 TypeStructure는 생성하되 typeInfo/methods는 null로 둔다")
  void resolve_returnsNulls_whenParsedFileIsNull() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(), RepoEntryType.FILE, "class A {}");

    // when
    TypeStructure result = resolver.resolve(sourceFile, null, Map.of());

    // then
    assertThat(result).isNotNull();
    assertThat(result.typeInfo()).isNull();
    assertThat(result.methods()).isNull();
  }

  @Test
  @DisplayName("parsedFile 결과와 매칭되는 bytecode 구조가 없으면 예외처리한다")
  void resolve_throws_whenBytecodeNotFound() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(), RepoEntryType.FILE, "class A {}");

    ParsedSourceFile parsedFile = new ParsedSourceFile("com/example/A", Map.of());

    // when & then
    assertThatThrownBy(() -> resolver.resolve(sourceFile, parsedFile, Map.of()))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("parsedFile 결과와 바이트코드 결과가 매칭되면 TypeStructure와 내부의 MethodStructure도 생성된다")
  void resolve_createsTypeStructure_andMethodStructures_whenMatched() {
    // given
    OpenSourceFileInfo sourceFile =
        new OpenSourceFileInfo(
            Path.of("/tmp/repo/A.java").toString(),
            RepoEntryType.FILE,
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
            typeInternalName, Map.of(MethodSignature.of(parseResult), parseResult));
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
