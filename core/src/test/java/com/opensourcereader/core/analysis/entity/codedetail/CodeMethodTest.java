package com.opensourcereader.core.analysis.entity.codedetail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.opensourcereader.core.analysis.dto.OpenSourceContentMethodExtractResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodeMethodTest {

  @Test
  @DisplayName("updateAllOutgoingCalls: 새 목록에 없는 edge는 제거되고, 없는 edge는 추가된다")
  void updateAllOutgoingCalls_shouldRemoveAndAdd() {
    // given
    CodeMethod callerCodeMethod = Fixtures.codeMethod("callerMethod");
    CodeMethod firstCalleeCodeMethod = Fixtures.codeMethod("firstCalleeMethod");
    CodeMethod secondCalleeCodeMethod = Fixtures.codeMethod("secondCalleeMethod");

    CodeMethodCallEdge callerToFirstCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, firstCalleeCodeMethod);
    CodeMethodCallEdge callerToSecondCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, secondCalleeCodeMethod);
    CodeMethodCallEdge firstCalleeToSecondCalleeEdge =
        new CodeMethodCallEdge(firstCalleeCodeMethod, secondCalleeCodeMethod);

    // 초기 상태: [callerToFirstCalleeEdge, callerToSecondCalleeEdge]
    callerCodeMethod.updateAllOutgoingCalls(
        List.of(callerToFirstCalleeEdge, callerToSecondCalleeEdge));
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToFirstCalleeEdge, callerToSecondCalleeEdge);

    // when: [callerToSecondCalleeEdge, firstCalleeToSecondCalleeEdge]로 업데이트
    callerCodeMethod.updateAllOutgoingCalls(
        List.of(callerToSecondCalleeEdge, firstCalleeToSecondCalleeEdge));

    // then: callerToFirstCalleeEdge 제거, firstCalleeToSecondCalleeEdge 추가 =>
    // [callerToSecondCalleeEdge, firstCalleeToSecondCalleeEdge]
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToSecondCalleeEdge, firstCalleeToSecondCalleeEdge);
  }

  @Test
  @DisplayName("updateAllOutgoingCalls: 기존에 있던 edge는 유지되고(중복 추가 X), 새 edge만 추가된다")
  void updateAllOutgoingCalls_shouldNotDuplicateExisting() {
    // given
    CodeMethod callerCodeMethod = Fixtures.codeMethod("callerMethod");
    CodeMethod firstCalleeCodeMethod = Fixtures.codeMethod("firstCalleeMethod");
    CodeMethod secondCalleeCodeMethod = Fixtures.codeMethod("secondCalleeMethod");

    CodeMethodCallEdge callerToFirstCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, firstCalleeCodeMethod);
    CodeMethodCallEdge callerToSecondCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, secondCalleeCodeMethod);

    callerCodeMethod.updateAllOutgoingCalls(
        List.of(callerToFirstCalleeEdge, callerToSecondCalleeEdge));
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToFirstCalleeEdge, callerToSecondCalleeEdge);

    // when: callerToSecondCalleeEdge를 중복 포함해서 넘겨도 (같은 인스턴스 기준)
    callerCodeMethod.updateAllOutgoingCalls(
        List.of(callerToSecondCalleeEdge, callerToSecondCalleeEdge, callerToFirstCalleeEdge));

    // then: 중복 추가는 일어나지 않고 기존 목록 유지
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToFirstCalleeEdge, callerToSecondCalleeEdge);
  }

  @Test
  @DisplayName("updateAllOutgoingCalls: 입력 리스트의 순서를 보장하지 않는다(기존 순서 유지 + 새 항목 뒤에 추가)")
  void updateAllOutgoingCalls_orderIsNotReorderedToMatchInput() {
    // given
    CodeMethod callerCodeMethod = Fixtures.codeMethod("callerMethod");
    CodeMethod firstCalleeCodeMethod = Fixtures.codeMethod("firstCalleeMethod");
    CodeMethod secondCalleeCodeMethod = Fixtures.codeMethod("secondCalleeMethod");

    CodeMethodCallEdge callerToFirstCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, firstCalleeCodeMethod);
    CodeMethodCallEdge callerToSecondCalleeEdge =
        new CodeMethodCallEdge(callerCodeMethod, secondCalleeCodeMethod);

    callerCodeMethod.updateAllOutgoingCalls(
        List.of(callerToFirstCalleeEdge, callerToSecondCalleeEdge));
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToFirstCalleeEdge, callerToSecondCalleeEdge);

    // when: 순서를 바꿔서 전달 + 새 edge 추가
    CodeMethodCallEdge firstCalleeToSecondCalleeEdge =
        new CodeMethodCallEdge(firstCalleeCodeMethod, secondCalleeCodeMethod);

    callerCodeMethod.updateAllOutgoingCalls(
        List.of(firstCalleeToSecondCalleeEdge, callerToSecondCalleeEdge));

    // then:
    // removeIf로 callerToFirstCalleeEdge 제거 => [callerToSecondCalleeEdge]
    // loop에서 firstCalleeToSecondCalleeEdge 추가 => [callerToSecondCalleeEdge,
    // firstCalleeToSecondCalleeEdge]
    // (입력 순서 [firstCalleeToSecondCalleeEdge, callerToSecondCalleeEdge]를 그대로 맞추진 않음)
    assertThat(callerCodeMethod.getOutgoingCalls())
        .containsExactly(callerToSecondCalleeEdge, firstCalleeToSecondCalleeEdge);
  }

  @Test
  @DisplayName("updateAllIngoingCalls도 동일한 규칙으로 동작한다")
  void updateAllIngoingCalls_shouldRemoveAndAdd() {
    // given
    CodeMethod calleeCodeMethod = Fixtures.codeMethod("calleeMethod");
    CodeMethod firstCallerCodeMethod = Fixtures.codeMethod("firstCallerMethod");
    CodeMethod secondCallerCodeMethod = Fixtures.codeMethod("secondCallerMethod");

    CodeMethodCallEdge firstCallerToCalleeEdge =
        new CodeMethodCallEdge(firstCallerCodeMethod, calleeCodeMethod);
    CodeMethodCallEdge secondCallerToCalleeEdge =
        new CodeMethodCallEdge(secondCallerCodeMethod, calleeCodeMethod);
    CodeMethodCallEdge firstCallerToSecondCallerEdge =
        new CodeMethodCallEdge(firstCallerCodeMethod, secondCallerCodeMethod);

    calleeCodeMethod.updateAllIngoingCalls(
        List.of(firstCallerToCalleeEdge, secondCallerToCalleeEdge));
    assertThat(calleeCodeMethod.getIngoingCalls())
        .containsExactly(firstCallerToCalleeEdge, secondCallerToCalleeEdge);

    // when
    calleeCodeMethod.updateAllIngoingCalls(
        List.of(secondCallerToCalleeEdge, firstCallerToSecondCallerEdge));

    // then
    assertThat(calleeCodeMethod.getIngoingCalls())
        .containsExactly(secondCallerToCalleeEdge, firstCallerToSecondCallerEdge);
  }

  static class Fixtures {

    static CodeMethod codeMethod(String methodName) {
      OpenSourceContentMethodExtractResult methodExtractResult =
          new OpenSourceContentMethodExtractResult(
              methodName, MethodModifier.PUBLIC, List.of(), 1, 1);

      return CodeMethod.of(methodExtractResult, null);
    }
  }
}
