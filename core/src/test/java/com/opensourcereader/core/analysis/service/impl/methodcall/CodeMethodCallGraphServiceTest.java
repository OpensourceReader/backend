package com.opensourcereader.core.analysis.service.impl.methodcall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;
import com.opensourcereader.core.analysis.testfixture.TestRepoFixtures;
import com.opensourcereader.core.analysis.testfixture.TestTypeFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class CodeMethodCallGraphServiceTest {

  @Autowired CodeMethodCallGraphService codeMethodCallGraphService;
  @Autowired DeclaredTypeRepository declaredTypeRepository;
  @Autowired CodeMethodRepository codeMethodRepository;
  @Autowired OpenSourceRepoRepository openSourceRepoRepository;

  @Transactional
  @Test
  @DisplayName("메서드 id로 조회하면 outgoing/ingoing 그래프를 함께 가져온다 (DeclaredType + DeclaredMethod + Edge만)")
  void getCodeMethodById_edgesOnly() {
    // given
    String callerTypeName = "t/Other";
    String callerMethodName = "run";
    String targetTypeName = "t/Main";
    String targetMethodName = "target";
    String calleeTypeName = "t/Util";
    String calleeMethodName = "help";
    MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

    // 메서드 하나씩 해서 선언을 가능함
    TypeStructure callerType =
        TestTypeFixtures.createTypeWithMethod(
            "caller",
            RepoEntryType.FILE,
            callerTypeName,
            callerMethodName,
            methodDescriptor,
            TypeKind.CLASS);
    TypeStructure targetType =
        TestTypeFixtures.createTypeWithMethod(
            "caller",
            RepoEntryType.FILE,
            targetTypeName,
            targetMethodName,
            methodDescriptor,
            TypeKind.CLASS);
    TypeStructure calleeType =
        TestTypeFixtures.createTypeWithMethod(
            "caller",
            RepoEntryType.FILE,
            calleeTypeName,
            calleeMethodName,
            methodDescriptor,
            TypeKind.CLASS);
    OpenSourceRepo repo =
        TestRepoFixtures.saveRepo(
            openSourceRepoRepository, "new-cloneUrl", List.of(callerType, targetType, calleeType));

    DeclaredMethod callerMethod =
        declaredTypeRepository
            .findByRepoAndTypeInternalName(repo.getId(), callerTypeName)
            .orElseThrow()
            .getDeclaredMethods()
            .get(0);
    DeclaredMethod targetMethod =
        declaredTypeRepository
            .findByRepoAndTypeInternalName(repo.getId(), targetTypeName)
            .orElseThrow()
            .getDeclaredMethods()
            .get(0);
    DeclaredMethod calleeMethod =
        declaredTypeRepository
            .findByRepoAndTypeInternalName(repo.getId(), calleeTypeName)
            .orElseThrow()
            .getDeclaredMethods()
            .get(0);
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
