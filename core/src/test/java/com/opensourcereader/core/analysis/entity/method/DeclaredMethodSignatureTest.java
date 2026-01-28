package com.opensourcereader.core.analysis.entity.method;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DeclaredMethodSignatureTest {

  @DisplayName("CodeMethodSignature는 JVM descriptor 기준으로 short name 시그니처를 생성한다")
  @ParameterizedTest(name = "[{index}] descriptor={0}")
  @MethodSource("methodSignatureTestCases")
  void codeSignatureParameterizedTest(String descriptor, String expectedSignature) {

    // given
    DeclaredMethodInfo methodExtractResult =
        new DeclaredMethodInfo(
            "internalName",
            "methodName",
            EnumSet.noneOf(MethodModifier.class),
            MethodDescriptor.from(descriptor),
            null,
            List.of(),
            null,
            null);

    // when
    CodeMethodSignature codeMethodSignature = CodeMethodSignature.of(methodExtractResult);

    // then
    Assertions.assertThat(codeMethodSignature.methodSignature()).isEqualTo(expectedSignature);
  }

  static Stream<Arguments> methodSignatureTestCases() {
    String expected = "methodName(String,OpenSourceRepo)void";

    return Stream.of(
        // 기본 객체 타입
        Arguments.of(
            "(Ljava/lang/String;Lcom/opensourcereader/core/analysis/entity/OpenSourceRepo;)V",
            expected),

        // 배열 케이스도 추가해보자 (현실성 있음)
        Arguments.of(
            "([Ljava/lang/String;Lcom/opensourcereader/core/analysis/entity/OpenSourceRepo;)V",
            "methodName(String[],OpenSourceRepo)void"),

        // primitive 포함
        Arguments.of("(ILjava/lang/String;)V", "methodName(int,String)void"));
  }
}
