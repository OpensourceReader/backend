package com.opensourcereader.core.analysis.service.impl;

import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithMethod;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeWithMethodCall;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class MethodCallGraphServiceTest {

  @Autowired OpenSourceRepoService openSourceRepoService;
  @Autowired MethodCallGraphService methodCallGraphService;

  @Transactional
  @Test
  @DisplayName("메서드 id로 조회하면 outgoing/ingoing 그래프를 함께 가져온다 (DeclaredType + DeclaredMethod + Edge만)")
  void getCodeMethodById_edgesOnly() {
    // given
    MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

    String callerTypeName = "t/Other";
    String callerMethodName = "run";
    String targetTypeName = "t/Main";
    String targetMethodName = "target";
    String calleeTypeName = "t/Util";
    String calleeMethodName = "help";
    TypeStructure callerType =
        createTypeWithMethodCall(
            TypeKind.CLASS,
            callerTypeName,
            callerMethodName,
            methodDescriptor,
            targetTypeName,
            targetMethodName,
            methodDescriptor);

    TypeStructure targetType =
        createTypeWithMethodCall(
            TypeKind.CLASS,
            targetTypeName,
            targetMethodName,
            methodDescriptor,
            calleeTypeName,
            calleeMethodName,
            methodDescriptor);

    TypeStructure calleeType =
        createTypeStructureWithMethod(
            calleeTypeName, null, List.of(), calleeMethodName, methodDescriptor, TypeKind.CLASS);

    OpenSourceRepo repo =
        openSourceRepoService.createRepo(
            "new-cloneUrl", List.of(callerType, targetType, calleeType));
    methodCallGraphService.create(repo.getTypes(), List.of(callerType, targetType, calleeType));
    Method targetMethod =
        repo.getTypes().stream()
            .flatMap(type -> type.getMethods().stream())
            .filter(
                method ->
                    method.getMethodName().equals(targetMethodName)
                        && method.getType().getTypeInternalName().equals(targetTypeName))
            .findFirst()
            .get();

    // when
    Method target = methodCallGraphService.getCodeMethodById(targetMethod.getId());

    // then
    assertThat(target.getOutgoingCalls())
        .extracting(e -> e.getCallee().getMethodName())
        .containsExactlyInAnyOrder(calleeMethodName);
    assertThat(target.getIngoingCalls())
        .extracting(e -> e.getCaller().getMethodName())
        .containsExactlyInAnyOrder(callerMethodName);
  }
}
