package com.opensourcereader.core.analysis.service.impl.callgraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class CodeMethodCallGraphServiceTest {

  @Autowired CodeMethodCallGraphService codeMethodCallGraphService;
  @Autowired DeclaredTypeRepository declaredTypeRepository;
  @Autowired CodeMethodRepository codeMethodRepository;

  @Transactional
  @Test
  @DisplayName("메서드 id로 조회하면 outgoing/ingoing 그래프를 함께 가져온다 (DeclaredType + DeclaredMethod + Edge만)")
  void getCodeMethodById_edgesOnly() {
    // given
    DeclaredType callerType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new ClassInfo(9, TypeKind.CLASS, "t/Other", null, null, null),
                List.of(
                    new CodeMethodExtractResult(
                        "run",
                        EnumSet.of(MethodModifier.PACKAGE_PRIVATE),
                        "void",
                        List.of("t.Main"),
                        3,
                        3)),
                null));
    DeclaredType targetType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new ClassInfo(9, TypeKind.CLASS, "t/Main", null, null, null),
                List.of(
                    new CodeMethodExtractResult(
                        "target",
                        EnumSet.of(MethodModifier.PACKAGE_PRIVATE),
                        "void",
                        List.of(),
                        3,
                        3)),
                null));
    DeclaredType calleeType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new ClassInfo(9, TypeKind.CLASS, "t/Util", null, null, null),
                List.of(
                    new CodeMethodExtractResult(
                        "help",
                        EnumSet.of(MethodModifier.STATIC, MethodModifier.PACKAGE_PRIVATE),
                        "void",
                        List.of(),
                        3,
                        3)),
                null));
    declaredTypeRepository.saveAll(List.of(callerType, targetType, calleeType));
    // given
    DeclaredMethod callerMethod = callerType.getDeclaredMethods().get(0);
    DeclaredMethod targetMethod = targetType.getDeclaredMethods().get(0);
    DeclaredMethod calleeMethod = calleeType.getDeclaredMethods().get(0);
    targetMethod.addIngoingCall(callerMethod);
    targetMethod.addOutgoingCall(calleeMethod);

    codeMethodRepository.saveAll(List.of(targetMethod, calleeMethod, callerMethod));

    // when
    DeclaredMethod target = codeMethodCallGraphService.getCodeMethodById(targetMethod.getId());

    // then
    assertThat(target.getOutgoingCalls())
        .extracting(e -> e.getCallee().getId(), e -> e.getCallee().getMethodName())
        .containsExactlyInAnyOrder(tuple(calleeMethod.getId(), "help"));
    assertThat(target.getIngoingCalls())
        .extracting(e -> e.getCaller().getId(), e -> e.getCaller().getMethodName())
        .containsExactlyInAnyOrder(tuple(callerMethod.getId(), "run"));
  }
}
