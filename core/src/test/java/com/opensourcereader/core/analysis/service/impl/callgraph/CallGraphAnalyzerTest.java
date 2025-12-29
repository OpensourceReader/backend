// package com.opensourcereader.core.analysis.service.impl.callgraph;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.SoftAssertions.assertSoftly;
//
// import java.util.List;
// import java.util.Map;
//
// import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
// import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
// import com.opensourcereader.core.analysis.infra.bytecode.CallGraphAnalyzer;
// import com.opensourcereader.core.analysis.testfixture.InMemoryJavaCompilerFixture;
// import org.assertj.core.groups.Tuple;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
//
// class CallGraphAnalyzerTest {
//
//  private final CallGraphAnalyzer analyzer = new CallGraphAnalyzer();
//
//  @Nested
//  @DisplayName("Constructor (<init>) 규칙")
//  class ConstructorRules {
//
//    @Test
//    @DisplayName("생성자가 하나도 없을 경우, 기본 생성자를 생성합니다.")
//    void callGraphAnalyzerTest_no_init() {
//      // given
//      String src =
//          """
//              package t;
//
//              public class Main {
//                void run() {}
//              }
//              """;
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile("t.Main", src);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//
//    @Test
//    @DisplayName("생성자를 하나라도 썻을떄, 해당 생성자를 참고합니다.(public/private무관)")
//    void callGraphAnalyzerTest_init() {
//      // given
//      String src =
//          """
//              package t;
//
//              public class Main {
//
//                private Main(int x) {
//                  // param constructor
//                }
//
//                void run() {}
//              }
//              """;
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile("t.Main", src);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//
//    @Test
//    @DisplayName("this(...) 생성자 체이닝은 같은 클래스(Main.<init>(int))로 추출된다")
//    void constructor_this_chaining_is_captured() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    public Main() { this(1); }
//                    public Main(int x) {}
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            assertThat(result.classPath()).isEqualTo("t/Main.java");
//            // Main() -> Main(int) 호출이 있어야 함
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Main.java", "<init>", "void", List.of("int")),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//  }
//
//  @Nested
//  @DisplayName("Static 호출 (invokestatic)")
//  class StaticCalls {
//
//    @Test
//    @DisplayName("static 호출과 생성자(Object.<init>) 호출이 콜그래프로 추출된다")
//    void captures_static_method_call_and_constructor_call() {
//      // given
//      String src =
//          """
//              package t;
//
//              class Util {
//                static void help() {}
//              }
//
//              public class Main {
//                void run() { Util.help(); }
//              }
//              """;
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile("t.Main", src);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Util.java", "help", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//
//    @Test
//    @DisplayName("오버로드 static 호출은 인자 타입(desc)으로 구분되어 각각 추출된다")
//    void overloaded_static_calls_are_distinguished_by_argument_types() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Util.java",
//              """
//                  package t;
//                  public class Util {
//                    public static void f(int x) {}
//                    public static void f(String s) {}
//                  }
//                  """,
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    void run() { Util.f(1); Util.f("a"); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Util.java", "f", "void", List.of("int")),
//                    Tuple.tuple(
//                        "t/Main.java", "t/Util.java", "f", "void", List.of("java.lang.String")),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//
//    @Test
//    @DisplayName("같은 클래스의 static 호출(s)은 Main.s로 추출된다")
//    void same_class_static_call_is_captured() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    static void s() {}
//                    void run() { s(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Main.java", "s", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//  }
//
//  @Nested
//  @DisplayName("다형성 호출 (interface / virtual / super)")
//  class PolymorphicDispatch {
//
//    @Test
//    @DisplayName("인터페이스 타입 호출(i.m)은 callee가 인터페이스(t/I)로 추출되고 구현체(t/A)로는 확정되지 않는다")
//    void interface_call_is_captured_as_interface_not_implementation() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/I.java",
//              """
//                  package t;
//                  public interface I { void m(); }
//                  """,
//              "t/A.java",
//              """
//                  package t;
//                  class A implements I {
//                    public void m() {}
//                  }
//                  """,
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    void run(I i) { i.m(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertThat(result.classPath()).isEqualTo("t/Main.java");
//
//      // 인터페이스 호출은 t/I.m 으로 찍히는 게 정상
//      assertSoftly(
//          softly -> {
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/I.java", "m", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName())
//                .doesNotContain(Tuple.tuple("t/A.java", "m"));
//          });
//    }
//
//    @Test
//    @DisplayName("super 호출(super.p)은 callee owner가 Parent로 추출된다")
//    void super_call_is_captured() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Parent.java",
//              """
//                  package t;
//                  class Parent { void p() {} }
//                  """,
//              "t/Child.java",
//              """
//                  package t;
//                  class Child extends Parent {
//                    void c() { super.p(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] childBytes = compiled.get("t.Child");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, childBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Child.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Child.java", "t/Parent.java", "p", "void", List.of()),
//                    Tuple.tuple("t/Child.java", "t/Parent.java", "<init>", "void", List.of()));
//          });
//    }
//
//    @Test
//    @DisplayName("가상 호출(x.p)은 컴파일타임 타입(Parent.p)으로 추출되고 구현체(Child.p)로는 확정되지 않는다")
//    void virtual_call_owner_is_compile_time_type() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Parent.java",
//              """
//                  package t;
//                  public class Parent { public void p() {} }
//                  """,
//              "t/Child.java",
//              """
//                  package t;
//                  public class Child extends Parent {
//                    @Override public void p() {}
//                  }
//                  """,
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    void run(Parent x) { x.p(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Parent.java", "p", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//            // 구현체(Child.p)로 확정되면 안 됨 (Raw edge 단계)
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName())
//                .doesNotContain(Tuple.tuple("t/Child.java", "p"));
//          });
//    }
//
//    @Test
//    @DisplayName("가상 호출(x.p)은 구현체로 타입을 선언하면 구현체(Child.p)로 확정된다.")
//    void im_call_owner_is_compile_time_type() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Parent.java",
//              """
//                  package t;
//                  public class Parent { public void p() {} }
//                  """,
//              "t/Child.java",
//              """
//                  package t;
//                  public class Child extends Parent {
//                    @Override public void p() {}
//                  }
//                  """,
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    void run(Child x) { x.p(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Child.java", "p", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//            // 구현체(Child.p)로 확정되면 안 됨 (Raw edge 단계)
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName())
//                .doesNotContain(Tuple.tuple("t/Parent.java", "p"));
//          });
//    }
//  }
//
//  @Nested
//  @DisplayName("Special 호출 (invokespecial) - private / 생성자")
//  class SpecialCalls {
//
//    @Test
//    @DisplayName("private 메서드 호출(x)은 같은 클래스(Main.x)로 추출된다")
//    void private_call_is_captured() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    private void x() {}
//                    void run() { x(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            softly.assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/Main.java", "x", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//  }
//
//  @Nested
//  @DisplayName("Object 생성 (new)")
//  class ObjectCreation {
//
//    @Test
//    @DisplayName("new A()는 A.<init> 호출로 추출된다")
//    void new_object_creation_is_captured_as_constructor_call() {
//      // given
//      Map<String, String> sources =
//          Map.of(
//              "t/A.java",
//              """
//                  package t;
//                  public class A {
//                    public A() {}
//                  }
//                  """,
//              "t/Main.java",
//              """
//                  package t;
//                  public class Main {
//                    void run() { new A(); }
//                  }
//                  """);
//
//      Map<String, byte[]> compiled = InMemoryJavaCompilerFixture.compile(sources);
//      byte[] mainBytes = compiled.get("t.Main");
//
//      // when
//      ClassStructure result =
//          analyzer.createMethodCallsOfClass(List.of(new ClassBytecode(null, mainBytes))).get(0);
//
//      // then
//      assertSoftly(
//          softly -> {
//            assertThat(result.classPath()).isEqualTo("t/Main.java");
//            softly
//                .assertThat(result.methodCallEdges())
//                .extracting(
//                    methodCallEdge -> methodCallEdge.caller().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().classPath(),
//                    methodCallEdge -> methodCallEdge.callee().methodName(),
//                    methodCallEdge -> methodCallEdge.callee().methodReturnType(),
//                    methodCallEdge -> methodCallEdge.callee().argumentTypes())
//                .containsExactlyInAnyOrder(
//                    Tuple.tuple("t/Main.java", "t/A.java", "<init>", "void", List.of()),
//                    Tuple.tuple(
//                        "t/Main.java", "java/lang/Object.java", "<init>", "void", List.of()));
//          });
//    }
//  }
// }
