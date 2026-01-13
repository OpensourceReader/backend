package com.opensourcereader.core.analysis.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.infra.parser.SourceCodeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SpringBootTest
class SourceCodeParserTest {

  @Autowired private SourceCodeParser sourceCodeParser;

  @Test
  @DisplayName("패키지명+클래스명칭을 클래스이름으로 반환합니다.")
  void extract_mapsSourceBySignature() {
    // given
    String rawText =
        """
        package com.example.ossr;
        public class A {
          public void m(String s) {}
          public void n() {}
        }
        """;
    String fqcn = sourceCodeParser.extractClassName(rawText);

    assertThat(fqcn).isEqualTo("com.example.ossr.A");
  }

  @Nested
  @DisplayName("유효성 검사 조건")
  class ValidationCase {

    @Test
    @DisplayName("빈 텍스트면 빈 리스트를 반환합니다.")
    void emptyText() {
      String rawText = "";

      assertThat(sourceCodeParser.extractCodeMethods(rawText)).isEmpty();
    }

    @Test
    @DisplayName("문법 오류면 빈 리스트를 반환합니다.")
    void parseFail() {
      String rawText = "public class A { void a( { }";

      assertThat(sourceCodeParser.extractCodeMethods(rawText)).isEmpty();
    }

    @Test
    @DisplayName("package/import만 있어도 메서드는 없습니다.")
    void packageOnly() {
      String rawText = """
          package a.b;
          import java.util.*;
          """;

      assertThat(sourceCodeParser.extractCodeMethods(rawText)).isEmpty();
    }
  }

  @Nested
  @DisplayName("클래스/멤버 메서드 기본 문법")
  class BasicMethodSyntax {

    @Test
    @DisplayName("기본/오버로드/throws/varargs/배열 파라미터")
    void basicOverloadThrowsVarargsArray() {
      String rawText =
          """
              import java.io.IOException;

              public class A {
                public void m1() {}
                private int m2(int a) { return a; }
                protected String m2(String a) { return a; }
                void m3(String... args) throws IOException {}
                int[] m4(int[] a, String[][] b) { return a; }
              }
              """;

      var results = sourceCodeParser.extractCodeMethods(rawText);

      assertMethods(
          results,
          expected("m1", MethodModifier.PUBLIC, List.of()),
          expected("m2", MethodModifier.PRIVATE, List.of("int")),
          expected("m2", MethodModifier.PROTECTED, List.of("String")),
          expected("m3", MethodModifier.PACKAGE_PRIVATE, List.of("String...")),
          expected("m4", MethodModifier.PACKAGE_PRIVATE, List.of("int[]", "String[][]")));
    }

    @Test
    @DisplayName("제네릭 메서드")
    void generics() {
      String rawText =
          """
              import java.util.*;

              public class G<T extends Number & Comparable<T>> {
                public <R extends CharSequence> R m1(R r) { return r; }
                public List<? super Integer> m2(List<? super Integer> in) { return in; }
                public Map<String, ? extends Number> m3() { return Map.of(); }
              }
              """;

      var results = sourceCodeParser.extractCodeMethods(rawText);

      assertMethods(
          results,
          expected("m1", MethodModifier.PUBLIC, List.of("R")),
          expected("m2", MethodModifier.PUBLIC, List.of("List")),
          expected("m3", MethodModifier.PUBLIC, List.of()));
    }
  }

  @Nested
  @DisplayName("인터페이스/애노테이션 타입/레코드/열거형")
  class NonClassTypeDeclarations {

    @Test
    @DisplayName("인터페이스 메서드")
    void interfaceMethods() {
      String rawText =
          """
              public interface I {
                void a();
                default int b() { return 1; }
                static String c() { return "x"; }
                private void d() {}
              }
              """;

      var results = sourceCodeParser.extractCodeMethods(rawText);

      assertMethods(
          results,
          expected("a", MethodModifier.PUBLIC, List.of()),
          expected("b", MethodModifier.PUBLIC, List.of()),
          expected("c", MethodModifier.PUBLIC, List.of()),
          expected("d", MethodModifier.PRIVATE, List.of()));
    }

    @Test
    @DisplayName("record 메서드")
    void recordMethods() {
      String rawText =
          """
              public record R(int a, int b) {
                public int sum() { return a + b; }
              }
              """;

      var results = sourceCodeParser.extractCodeMethods(rawText);

      assertMethods(results, expected("sum", MethodModifier.PUBLIC, List.of()));
    }
  }

  private static ExpectedMethod expected(
      String name, MethodModifier modifier, List<String> paramTypes) {
    return new ExpectedMethod(name, modifier, paramTypes);
  }

  private static void assertMethods(
      List<SourceCodeParseResult> results, ExpectedMethod... expected) {

    assertThat(results)
        .extracting(
            SourceCodeParseResult::methodName,
            SourceCodeParseResult::modifier,
            SourceCodeParseResult::argumentTypes)
        .containsExactlyInAnyOrder(
            List.of(expected).stream()
                .map(e -> tuple(e.name, e.modifier, e.paramTypes))
                .toArray(org.assertj.core.groups.Tuple[]::new));
  }

  private record ExpectedMethod(String name, MethodModifier modifier, List<String> paramTypes) {}
}
