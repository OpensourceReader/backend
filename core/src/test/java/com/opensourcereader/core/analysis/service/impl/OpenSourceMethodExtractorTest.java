package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.OpenSourceContentMethodExtractResult;
import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.entity.codedetail.MethodAccessModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SpringBootTest
class OpenSourceMethodExtractorTest {

  @Autowired private OpenSourceMethodExtractor extractor;

  @Nested
  @DisplayName("유효성 검사 조건")
  class ValidationCase {

    @Test
    @DisplayName("자바 파일이 아니면 빈 리스트를 반환합니다.")
    void notJavaFile() {
      var file =
          new OpenSourceFileInfo(
              "README.md", ContentType.FILE.getTypeNumber(), "class A { void a(){} }");

      assertThat(extractor.extractCodeMethods(file)).isEmpty();
    }

    @Test
    @DisplayName("빈 텍스트면 빈 리스트를 반환합니다.")
    void emptyText() {
      var file = new OpenSourceFileInfo("A.java", ContentType.FILE.getTypeNumber(), "");

      assertThat(extractor.extractCodeMethods(file)).isEmpty();
    }

    @Test
    @DisplayName("문법 오류면 빈 리스트를 반환합니다.")
    void parseFail() {
      String broken = "public class A { void a( { }";
      var file = new OpenSourceFileInfo("A.java", ContentType.FILE.getTypeNumber(), broken);

      assertThat(extractor.extractCodeMethods(file)).isEmpty();
    }

    @Test
    @DisplayName("package/import만 있어도 메서드는 없습니다.")
    void packageOnly() {
      String code = """
          package a.b;
          import java.util.*;
          """;

      var file = new OpenSourceFileInfo("A.java", ContentType.FILE.getTypeNumber(), code);

      assertThat(extractor.extractCodeMethods(file)).isEmpty();
    }
  }

  @Nested
  @DisplayName("클래스/멤버 메서드 기본 문법")
  class BasicMethodSyntax {

    @Test
    @DisplayName("기본/오버로드/throws/varargs/배열 파라미터")
    void basicOverloadThrowsVarargsArray() {
      String code =
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

      var results =
          extractor.extractCodeMethods(
              new OpenSourceFileInfo("A.java", ContentType.FILE.getTypeNumber(), code));

      assertMethods(
          results,
          expected("m1", MethodAccessModifier.PUBLIC, List.of()),
          expected("m2", MethodAccessModifier.PRIVATE, List.of("int")),
          expected("m2", MethodAccessModifier.PROTECTED, List.of("String")),
          expected("m3", MethodAccessModifier.PACKAGE_PRIVATE, List.of("String...")),
          expected("m4", MethodAccessModifier.PACKAGE_PRIVATE, List.of("int[]", "String[][]")));
    }

    @Test
    @DisplayName("제네릭 메서드")
    void generics() {
      String code =
          """
          import java.util.*;

          public class G<T extends Number & Comparable<T>> {
            public <R extends CharSequence> R m1(R r) { return r; }
            public List<? super Integer> m2(List<? super Integer> in) { return in; }
            public Map<String, ? extends Number> m3() { return Map.of(); }
          }
          """;

      var results =
          extractor.extractCodeMethods(
              new OpenSourceFileInfo("G.java", ContentType.FILE.getTypeNumber(), code));

      assertMethods(
          results,
          expected("m1", MethodAccessModifier.PUBLIC, List.of("R")),
          expected("m2", MethodAccessModifier.PUBLIC, List.of("List")),
          expected("m3", MethodAccessModifier.PUBLIC, List.of()));
    }
  }

  @Nested
  @DisplayName("인터페이스/애노테이션 타입/레코드/열거형")
  class NonClassTypeDeclarations {

    @Test
    @DisplayName("인터페이스 메서드")
    void interfaceMethods() {
      String code =
          """
          public interface I {
            void a();
            default int b() { return 1; }
            static String c() { return "x"; }
            private void d() {}
          }
          """;

      var results =
          extractor.extractCodeMethods(
              new OpenSourceFileInfo("I.java", ContentType.FILE.getTypeNumber(), code));

      assertMethods(
          results,
          expected("a", MethodAccessModifier.PUBLIC, List.of()),
          expected("b", MethodAccessModifier.PUBLIC, List.of()),
          expected("c", MethodAccessModifier.PUBLIC, List.of()),
          expected("d", MethodAccessModifier.PRIVATE, List.of()));
    }

    @Test
    @DisplayName("record 메서드")
    void recordMethods() {
      String code =
          """
          public record R(int a, int b) {
            public int sum() { return a + b; }
          }
          """;

      var results =
          extractor.extractCodeMethods(
              new OpenSourceFileInfo("R.java", ContentType.FILE.getTypeNumber(), code));

      assertMethods(results, expected("sum", MethodAccessModifier.PUBLIC, List.of()));
    }
  }

  private static ExpectedMethod expected(
      String name, MethodAccessModifier modifier, List<String> paramTypes) {
    return new ExpectedMethod(name, modifier, paramTypes);
  }

  private static void assertMethods(
      List<OpenSourceContentMethodExtractResult> results, ExpectedMethod... expected) {

    assertThat(results)
        .extracting(
            OpenSourceContentMethodExtractResult::methodName,
            OpenSourceContentMethodExtractResult::modifier,
            OpenSourceContentMethodExtractResult::paramTypes)
        .containsExactlyInAnyOrder(
            List.of(expected).stream()
                .map(e -> tuple(e.name, e.modifier, e.paramTypes))
                .toArray(org.assertj.core.groups.Tuple[]::new));
  }

  private record ExpectedMethod(
      String name, MethodAccessModifier modifier, List<String> paramTypes) {}
}
