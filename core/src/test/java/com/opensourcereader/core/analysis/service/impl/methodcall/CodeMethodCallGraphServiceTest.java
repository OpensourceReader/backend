package com.opensourcereader.core.analysis.service.impl.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getDeclaredMethodInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
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
    MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

    String callerClassName = "t/Other";
    String callerMethodName = "run";
    DeclaredType callerType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new TypeInfo(9, TypeKind.CLASS, callerClassName, null, null, null),
                List.of(getDeclaredMethodInfo(callerClassName, callerMethodName, methodDescriptor)),
                null));

    String targetClassName = "t/Main";
    DeclaredType targetType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new TypeInfo(9, TypeKind.CLASS, targetClassName, null, null, null),
                List.of(getDeclaredMethodInfo(targetClassName, "target", methodDescriptor)),
                null));

    String calleeClassName = "t/Util";
    String calleeMethodName = "help";
    DeclaredType calleeType =
        declaredTypeRepository.save(
            DeclaredType.internal(
                new TypeInfo(9, TypeKind.CLASS, calleeClassName, null, null, null),
                List.of(getDeclaredMethodInfo(targetClassName, calleeMethodName, methodDescriptor)),
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
        .containsExactlyInAnyOrder(tuple(calleeMethod.getId(), calleeMethodName));
    assertThat(target.getIngoingCalls())
        .extracting(e -> e.getCaller().getId(), e -> e.getCaller().getMethodName())
        .containsExactlyInAnyOrder(tuple(callerMethod.getId(), callerMethodName));
  }
}
