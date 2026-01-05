// package com.opensourcereader.core.analysis.service.impl;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import com.opensourcereader.core.analysis.entity.repo.ContentType;
// import java.nio.file.Path;
// import java.util.EnumSet;
// import java.util.List;
// import java.util.Map;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
// import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
// import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
// import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
// import com.opensourcereader.core.analysis.entity.method.AccessModifier;
// import com.opensourcereader.core.analysis.entity.method.NonAccessModifier;
// import com.opensourcereader.core.analysis.infra.bytecode.ClassStructureExtractor;
// import com.opensourcereader.core.analysis.testfixture.InMemoryJavaCompilerFixture;
// import org.assertj.core.groups.Tuple;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
//
// @SpringBootTest
// class CodeMethodExtractorTest {
//
//  @Autowired private CodeMethodExtractor extractor;
//  @Autowired private ClassStructureExtractor classStructureExtractor;
//
//  @Test
//  @DisplayName("java 파일이 아니면 빈 리스트")
//  void extract_nonJava_returnsEmpty() {
//    // given
//    OpenSourceFileInfo file = new OpenSourceFileInfo("README.md", ContentType.FILE, "anything");
//
//    // when
//    var results = extractor.extract(file, null);
//
//    // then
//    assertThat(results).isEmpty();
//  }
//
//  @Test
//  @DisplayName("바이트코드 메서드 목록을 기준으로 소스 파싱 결과를 매핑 후, start-endLine을 기록합니다.")
//  void extract_mapsSourceBySignature() {
//    // given
//    String className = "com.example.ossr.A";
//    String rawText =
//        """
//            package com.example.ossr;
//            public class A {
//              public void m(String s) {}
//              public void n() {}
//            }
//            """;
//    Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(className, rawText);
//    byte[] mainBytes = compiled.get(className);
//    OpenSourceFileInfo file = new OpenSourceFileInfo(className + ".java", ContentType.FILE,
// rawText);
//
//    List<ClassBytecode> classBytecodes = List.of(new ClassBytecode(Path.of(className),
// mainBytes));
//    List<ClassStructure> methodCallsOfClass = classStructureExtractor.extract(classBytecodes);
//
//    // when
//    List<CodeMethodExtractResult> extractResult =
//        extractor.extract(file, methodCallsOfClass.get(0));
//
//    // then
//    assertThat(extractResult)
//        .extracting(
//            CodeMethodExtractResult::methodName,
//            CodeMethodExtractResult::modifier,
//            CodeMethodExtractResult::nonAccessModifiers,
//            CodeMethodExtractResult::paramTypes,
//            CodeMethodExtractResult::startLine,
//            CodeMethodExtractResult::endLine)
//        .containsExactlyInAnyOrder(
//            Tuple.tuple(
//                "<init>",
//                AccessModifier.PUBLIC,
//                EnumSet.noneOf(NonAccessModifier.class),
//                List.of(),
//                null,
//                null),
//            Tuple.tuple(
//                "m",
//                AccessModifier.PUBLIC,
//                EnumSet.noneOf(NonAccessModifier.class),
//                List.of("java.lang.String"),
//                3,
//                3),
//            Tuple.tuple(
//                "n",
//                AccessModifier.PUBLIC,
//                EnumSet.noneOf(NonAccessModifier.class),
//                List.of(),
//                4,
//                4));
//  }
// }
