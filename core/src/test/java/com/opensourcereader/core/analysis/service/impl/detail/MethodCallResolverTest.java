package com.opensourcereader.core.analysis.service.impl.detail;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeWithMethod;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeWithMethodCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.file.RepoEntryType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
@Transactional
class MethodCallResolverTest {

  @Autowired MethodRepository methodRepository;
  @Autowired TypeRepository typeRepository;
  @Autowired MethodCallResolver methodCallResolver;
  @Autowired OpenSourceRepoRepository openSourceRepoRepository;

  @Test
  @DisplayName("Callee 메서드가 존재하면 → Caller DeclaredMethod와 연결된다")
  void givenExistingCalleeMethod_whenCreate_thenLinkResolvedMethod() {
    // given
    String calleeClassName = "b/B";
    String calleeMethodName = "b";
    MethodDescriptor calleeMethodDescriptor = MethodDescriptor.from("()V");
    TypeStructure calleeTypeStructure =
        createTypeWithMethod(
            "callee",
            RepoEntryType.FILE,
            calleeClassName,
            calleeMethodName,
            calleeMethodDescriptor,
            TypeKind.CLASS);

    String callerClassName = "a/A";
    String callerMethodName = "a";
    MethodDescriptor callerMethodDescriptor = MethodDescriptor.from("()V");
    TypeStructure callerTypeStructure =
        createTypeWithMethodCall(
            TypeKind.CLASS,
            callerClassName,
            callerMethodName,
            callerMethodDescriptor,
            calleeClassName,
            calleeMethodName,
            calleeMethodDescriptor);
    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository.save(
            OpenSourceRepo.of("new-cloneUrl", List.of(callerTypeStructure, calleeTypeStructure)));

    // when
    methodCallResolver.create(
        openSourceRepo.getId(),
        TypeStructureMeta.from(List.of(calleeTypeStructure, callerTypeStructure)));

    // then
    List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methodRepository.findAll());
    assertThat(outgoingCalls)
        .extracting(
            e -> e.getCaller().getTypeInternalName(),
            e -> e.getCaller().getMethodName(),
            e -> e.getCallee().getTypeInternalName(),
            e -> e.getCallee().getMethodName())
        .contains(tuple(callerClassName, callerMethodName, calleeClassName, calleeMethodName));
  }

  @Test
  @DisplayName("Callee 메서드는 없지만 Type이 내부에 존재하면 → internalInheritance placeholder로 연결된다")
  void shouldCreateInternalInheritancePlaceholderWhenTypeExists() {}

  @Test
  @DisplayName("Callee 메서드도 없고 Type도 없으면 → external placeholder로 연결된다")
  void shouldCreateExternalPlaceholderWhenTypeNotExists() {}
}
