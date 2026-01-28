package com.opensourcereader.core.analysis.service.impl.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.method.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
@Transactional
class MethodCallResolverTest {

  @Autowired MethodCallResolver resolver;
  @Autowired CodeMethodRepository codeMethodRepository;
  @Autowired DeclaredTypeRepository declaredTypeRepository;
  @Autowired OpenSourceRepoRepository openSourceRepoRepository;

  @Test
  @DisplayName("Callee 메서드가 존재하면 → Caller DeclaredMethod와 연결된다")
  void givenExistingCalleeMethod_whenCreate_thenLinkResolvedMethod() {
    // given
    String calleeClassName = "b/B";
    String calleeMethodName = "b";
    MethodDescriptor calleeMethodDescriptor = MethodDescriptor.from("()V");
    TypeInfo calleeTypeInfo =
        new TypeInfo(183, TypeKind.CLASS, calleeClassName, null, null, List.of());
    TypeStructure calleeTypeStructure =
        new TypeStructure(
            "callee",
            RepoEntryType.FILE,
            null,
            calleeTypeInfo,
            List.of(
                new MethodStructure(
                    new DeclaredMethodInfo(
                        calleeClassName,
                        calleeMethodName,
                        EnumSet.of(MethodModifier.PUBLIC),
                        calleeMethodDescriptor,
                        null,
                        null,
                        1,
                        1),
                    List.of())));

    String callerClassName = "a/A";
    String callerMethodName = "a";
    MethodDescriptor callerMethodDescriptor = MethodDescriptor.from("()V");
    TypeInfo callerTypeInfo =
        new TypeInfo(183, TypeKind.CLASS, callerClassName, null, null, List.of());
    TypeStructure callerTypeStructure =
        new TypeStructure(
            "caller",
            RepoEntryType.FILE,
            null,
            callerTypeInfo,
            List.of(
                new MethodStructure(
                    new DeclaredMethodInfo(
                        callerClassName,
                        callerMethodName,
                        EnumSet.of(MethodModifier.PUBLIC),
                        callerMethodDescriptor,
                        null,
                        null,
                        1,
                        1),
                    List.of(
                        new MethodCallInfo(
                            9,
                            calleeClassName,
                            calleeMethodName,
                            calleeMethodDescriptor,
                            false)))));

    OpenSourceRepo openSourceRepo =
        OpenSourceRepo.of("new-cloneUrl", List.of(calleeTypeStructure, callerTypeStructure));
    openSourceRepoRepository.save(openSourceRepo);

    // when
    List<DeclaredMethod> declaredMethods =
        resolver.create(
            openSourceRepo.getId(),
            TypeStructureMeta.from(List.of(calleeTypeStructure, callerTypeStructure)));

    // then
    List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
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
