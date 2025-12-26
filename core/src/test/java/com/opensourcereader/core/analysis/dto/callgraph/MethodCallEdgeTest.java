package com.opensourcereader.core.analysis.dto.callgraph;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MethodCallEdgeTest {

  @Test
  @DisplayName("of(): 클래스에 .java 확장자 붙이고 descriptor에서 반환/인자 타입 추출")
  void of_shouldCreateEdgeWithParsedTypes_usingExtracting() {
    // given
    String callerClassName = "com/example/Foo";
    String callerMethodName = "caller";
    String callerDescriptor = "(Ljava/lang/String;I)Ljava/util/List;"; // (String, int) -> List

    String calleeClassName = "com/example/Bar";
    String calleeMethodName = "callee";
    String calleeDescriptor = "(Ljava/util/Map;)V"; // (Map) -> void

    int opcode = 182; // e.g. INVOKEVIRTUAL
    boolean isInterface = false;

    // when
    MethodCallEdge edge =
        MethodCallEdge.of(
            callerClassName,
            callerMethodName,
            callerDescriptor,
            calleeClassName,
            calleeMethodName,
            calleeDescriptor,
            opcode,
            isInterface);

    // then
    SoftAssertions softly = new SoftAssertions();

    softly
        .assertThat(edge)
        .extracting(
            methodCallEdge -> methodCallEdge.caller().classPath(),
            methodCallEdge -> methodCallEdge.caller().methodName(),
            methodCallEdge -> methodCallEdge.caller().methodReturnType(),
            methodCallEdge -> methodCallEdge.caller().argumentTypes(),
            methodCallEdge -> methodCallEdge.callee().classPath(),
            methodCallEdge -> methodCallEdge.callee().methodName(),
            methodCallEdge -> methodCallEdge.callee().methodReturnType(),
            methodCallEdge -> methodCallEdge.callee().argumentTypes(),
            MethodCallEdge::operationCode,
            MethodCallEdge::isCalleeMethodInterface)
        .containsExactly(
            "com/example/Foo.java",
            "caller",
            "java.util.List",
            java.util.List.of("java.lang.String", "int"),
            "com/example/Bar.java",
            "callee",
            "void",
            java.util.List.of("java.util.Map"),
            opcode,
            isInterface);
  }

  @Test
  @DisplayName("of(): 인자가 없는 descriptor 처리")
  void of_shouldHandleNoArgsDescriptor_usingExtracting() {
    // given
    String callerDescriptor = "()I"; // () -> int
    String calleeDescriptor = "()Ljava/lang/String;"; // () -> String

    // when
    MethodCallEdge edge =
        MethodCallEdge.of(
            "a/b/C", "m1", callerDescriptor, "x/y/Z", "m2", calleeDescriptor, 184, true);

    // then
    SoftAssertions softly = new SoftAssertions();

    softly
        .assertThat(edge)
        .extracting(
            methodCallEdge -> methodCallEdge.caller().classPath(),
            methodCallEdge -> methodCallEdge.caller().argumentTypes(),
            methodCallEdge -> methodCallEdge.caller().methodReturnType(),
            methodCallEdge -> methodCallEdge.callee().classPath(),
            methodCallEdge -> methodCallEdge.callee().argumentTypes(),
            methodCallEdge -> methodCallEdge.callee().methodReturnType(),
            MethodCallEdge::isCalleeMethodInterface)
        .containsExactly(
            "a/b/C.java",
            java.util.List.of(),
            "int",
            "x/y/Z.java",
            java.util.List.of(),
            "java.lang.String",
            true);

    softly.assertAll();
  }

  @Test
  @DisplayName("of(): 배열 타입 descriptor 처리")
  void of_shouldHandleArrayTypes_usingExtracting() {
    // given
    String callerDescriptor =
        "([I[[Ljava/lang/String;)[Ljava/lang/String;"; // (int[], String[][]) -> String[]
    String calleeDescriptor = "([[I)V"; // (int[][]) -> void

    // when
    MethodCallEdge edge =
        MethodCallEdge.of(
            "p/Q", "caller", callerDescriptor, "r/S", "callee", calleeDescriptor, 185, false);

    // then
    SoftAssertions softly = new SoftAssertions();

    softly
        .assertThat(edge)
        .extracting(
            methodCallEdge -> methodCallEdge.caller().argumentTypes(),
            methodCallEdge -> methodCallEdge.caller().methodReturnType(),
            methodCallEdge -> methodCallEdge.callee().argumentTypes(),
            methodCallEdge -> methodCallEdge.callee().methodReturnType())
        .containsExactly(
            java.util.List.of("int[]", "java.lang.String[][]"),
            "java.lang.String[]",
            java.util.List.of("int[][]"),
            "void");

    softly.assertAll();
  }
}
