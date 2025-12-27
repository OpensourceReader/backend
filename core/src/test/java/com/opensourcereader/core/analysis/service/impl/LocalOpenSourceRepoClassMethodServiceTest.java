package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.MethodCallEdge;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryLoader;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoMethodCallAnalyzer;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class LocalOpenSourceRepoClassMethodServiceTest {

  @Autowired private GitRepositoryLoader gitRepositoryLoader;
  @Autowired private OpenSourceRepoService openSourceRepoService;
  @Autowired private OpenSourceRepoMethodCallAnalyzer openSourceRepoMethodCallAnalyzer;
  @Autowired private OpenSourceRepoClassMethodService openSourceRepoClassMethodService;

  @Autowired private CodeMethodRepository codeMethodRepository;

  @Transactional
  @DisplayName("outgoing 저장 확인 (A.m(String) -> B.n(int))")
  @Test
  void outgoingPersistCaseTest() {
    // given
    MethodCallsOfClass outgoingPersistCase =
        new MethodCallsOfClass(
            "com/acme/A.java",
            List.of(),
            List.of(
                MethodCallEdge.of(
                    "com/acme/A.java",
                    "m",
                    "(Ljava/lang/String;)V",
                    "com/acme/B.java",
                    "n",
                    "(I)V",
                    183,
                    false)));

    // when
    openSourceRepoClassMethodService.createMethodCallGraph(List.of(outgoingPersistCase));

    // then
  }

  @DisplayName("ingoing 검증 (B.n(int)를 A.m(String), C.p() 두 군데서 호출)")
  @Test
  void ingoingCaseTest() {
    // given
    MethodCallsOfClass ingoingCaseA =
        new MethodCallsOfClass(
            "com/acme/A.java",
            List.of(),
            List.of(
                MethodCallEdge.of(
                    "com/acme/A.java",
                    "m",
                    "(Ljava/lang/String;)V",
                    "com/acme/B.java",
                    "n",
                    "(I)V",
                    182, // INVOKEVIRTUAL
                    false)));

    MethodCallsOfClass ingoingCaseC =
        new MethodCallsOfClass(
            "com/acme/C.java",
            List.of(),
            List.of(
                MethodCallEdge.of(
                    "com/acme/C.java",
                    "p",
                    "()V",
                    "com/acme/B.java",
                    "n",
                    "(I)V",
                    182, // INVOKEVIRTUAL
                    false)));

    // when

    // then
  }

  @DisplayName("인터페이스 연결 (PayService implements IPayService)")
  @Test
  void interfaceCaseTest() {
    // given
    MethodCallsOfClass interfaceLinkCase =
        new MethodCallsOfClass(
            "com/acme/PayService.java",
            List.of("com/acme/IPayService.java"), // linkedInterfacePaths
            List.of(
                MethodCallEdge.of(
                    "com/acme/PayService.java",
                    "pay",
                    "(Ljava/lang/String;)V",
                    "com/acme/Dep.java",
                    "charge",
                    "(I)V",
                    182, // INVOKEVIRTUAL
                    false)));

    // when

    // then
  }

  @DisplayName("상속(override 결과가 Child.foo()로 찍히는 edge)")
  @Test
  void inheritanceCaseTest() {
    // given
    MethodCallsOfClass inheritanceCase =
        new MethodCallsOfClass(
            "com/acme/Caller.java",
            List.of(), // linkedInterfacePaths
            List.of(
                MethodCallEdge.of(
                    "com/acme/Caller.java",
                    "run",
                    "()V",
                    "com/acme/Child.java",
                    "foo",
                    "()V",
                    182, // INVOKEVIRTUAL
                    false)));

    // when

    // then
  }
}
