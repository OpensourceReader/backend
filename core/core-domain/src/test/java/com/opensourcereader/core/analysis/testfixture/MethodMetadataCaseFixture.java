package com.opensourcereader.core.analysis.testfixture;

import java.util.Map;

public final class MethodMetadataCaseFixture {

  private MethodMetadataCaseFixture() {}

  public static final Map<String, String> CASE_1_1_OVERLOAD =
      Map.of(
          "src/main/java/sample/case11/OverloadCase.java",
          """
          package sample.case11;

          public class OverloadCase {
            public void foo(int x) {}
            public void foo(Integer x) {}

            public void foo(String s) {}
            public void foo(String... xs) {} // varargs => String[] 디스크립터로 컴파일됨
          }
          """);

  public static final Map<String, String> CASE_1_2_NESTED =
      Map.of(
          "src/main/java/sample/case12/NestedCase.java",
          """
          package sample.case12;

          public class NestedCase {

            // static nested
            public static class StaticInner {
              public void m1() {}
            }

            // inner
            public class Inner {
              public void m2() {}
            }

            public void run() {
              // local class
              class Local {
                void m3() {}
              }
              new Local().m3();

              // anonymous class (컴파일되면 보통 NestedCase$1.class)
              Runnable r = new Runnable() {
                @Override public void run() {}
                public void m4() {}
              };
              r.run();
            }
          }
          """);

  public static final Map<String, String> CASE_1_3_GENERICS_ERASURE =
      Map.of(
          "src/main/java/sample/case13/GenericErasureCase.java",
          """
          package sample.case13;

          import java.util.List;

          public class GenericErasureCase {

            public void takeListOfString(List<String> xs) {}

            public List<String> returnsStringList() { return List.of(); }

            public <T extends Number> T id(T x) { return x; } // generic signature 영역 확인용
          }
          """);

  public static final Map<String, String> CASE_1_4_MODIFIERS_ANNOTATIONS =
      Map.of(
          "src/main/java/sample/case14/Anno.java",
          """
          package sample.case14;

          import java.lang.annotation.*;

          @Retention(RetentionPolicy.RUNTIME)
          @Target({ElementType.METHOD, ElementType.TYPE})
          public @interface Anno {
            String value() default "";
          }
          """,
          "src/main/java/sample/case14/ModifierCase.java",
          """
          package sample.case14;

          public class ModifierCase {

            @Anno("public")
            public void pub() {}

            @Anno("protected")
            protected void prot() {}

            @Anno("package-private")
            void pkg() {}

            @Anno("private")
            private void priv() {}

            public static void st() {}
          }
          """,
          "src/main/java/sample/case14/InterfaceModifierCase.java",
          """
          package sample.case14;

          public interface InterfaceModifierCase {

            default void def() {
              helperPrivate(); // private interface method (JDK9+)
            }

            static void st() {}

            private void helperPrivate() {}

            // protected는 인터페이스에 불가, package-private도 "멤버 메서드"로는 불가 (컴파일 규칙상)
          }
          """);

  public static final Map<String, String> CASE_1_5_RECORD_ENUM_SEALED =
      Map.of(
          "src/main/java/sample/case15/RecordCase.java",
          """
          package sample.case15;

          public record RecordCase(String name, int age) {
            // canonical ctor / accessor / equals/hashCode/toString 는 컴파일러가 생성
            public RecordCase {
              if (name == null) throw new IllegalArgumentException("name");
            }
          }
          """,
          "src/main/java/sample/case15/EnumCase.java",
          """
          package sample.case15;

          public enum EnumCase {
            A, B;

            public void hello() {}
            // values(), valueOf(String) 는 컴파일러가 생성
          }
          """,
          "src/main/java/sample/case15/SealedCase.java",
          """
          package sample.case15;

          public sealed interface SealedCase permits Cat, Dog {}

          final class Cat implements SealedCase {}
          final class Dog implements SealedCase {}
          """);
}
