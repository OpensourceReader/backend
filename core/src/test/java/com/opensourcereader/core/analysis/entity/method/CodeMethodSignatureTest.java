package com.opensourcereader.core.analysis.entity.method;

import java.util.List;
import java.util.stream.Stream;

import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CodeMethodSignatureTest {

  @DisplayName("MethodSignature 생성 테스트 - 파라미터 타입 포맷에 관계없이 short name으로 생성된다")
  @ParameterizedTest(name = "[{index}] rawParamTypes={0}")
  @MethodSource("methodSignatureTestCases")
  void codeSignatureParameterizedTest(List<String> rawParamTypes, String expectedSignature) {
    // given
    CodeMethodExtractResult methodExtractResult =
        new CodeMethodExtractResult("methodName", null, null, "void", rawParamTypes, null, null);

    // when
    CodeMethodSignature codeMethodSignature = CodeMethodSignature.of(methodExtractResult);

    // then
    Assertions.assertThat(codeMethodSignature.methodSignature()).isEqualTo(expectedSignature);
  }

  static Stream<Arguments> methodSignatureTestCases() {
    String methodName = "methodName";
    String expected = methodName + "(String,OpenSourceRepo)void";

    return Stream.of(
        // 1) 단순 클래스명
        Arguments.of(List.of("String", "OpenSourceRepo"), expected),

        // 2) JVM internal name ( / )
        Arguments.of(
            List.of("java/lang/String", "com/opensourcereader/core/analysis/entity/OpenSourceRepo"),
            expected),

        // 3) FQN ( . )
        Arguments.of(
            List.of(
                "java.lang.String",
                "com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo"),
            expected));
  }
}
