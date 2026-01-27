package com.opensourcereader.core.analysis.service.impl.callgraph;

import static com.opensourcereader.core.analysis.service.impl.callgraph.InterfacePolymorphicDispatcherTest.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
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
    String cloneUrl = "new-cloneUrl";
    OpenSourceRepo openSourceRepo = new OpenSourceRepo(cloneUrl);

    OpenSourceFileInfo calleeOpenFile = new OpenSourceFileInfo("callee", RepoEntryType.FILE, null);
    String calleeMethodName = "b";
    MethodDescriptor calleeMethodDescriptor = MethodDescriptor.from("()V");
    String calleeClassName = "b/B";
    ClassStructure calleeStructure =
        new ClassStructure(
            new ClassInfo(183, TypeKind.CLASS, calleeClassName, null, null, List.of()), List.of());
    CodeMethodExtractResult calleeMethodExtractResult =
        new CodeMethodExtractResult(
            calleeMethodName,
            EnumSet.of(MethodModifier.PUBLIC),
            calleeMethodDescriptor.methodReturnType(),
            calleeMethodDescriptor.argumentTypes(),
            1,
            1);
    OpenSourceRepoContent calleeContent =
        OpenSourceRepoContent.of(
            calleeOpenFile,
            calleeStructure.classInfo(),
            List.of(calleeMethodExtractResult),
            openSourceRepo);

    OpenSourceFileInfo callerOpenFile = new OpenSourceFileInfo("caller", RepoEntryType.FILE, null);
    String callerMethodName = "a";
    MethodDescriptor callerMethodDescriptor = MethodDescriptor.from("()V");
    String callerClassName = "a/A";
    MethodStructure methodStructure =
        new MethodStructure(
            new DeclaredMethodInfo(
                callerClassName,
                EnumSet.of(MethodModifier.PUBLIC),
                callerMethodName,
                callerMethodDescriptor,
                null,
                null),
            List.of(
                new MethodCallInfo(
                    9, calleeClassName, calleeMethodName, calleeMethodDescriptor, false)));
    ClassStructure callerStructure =
        new ClassStructure(
            new ClassInfo(183, TypeKind.CLASS, callerClassName, null, null, List.of()),
            List.of(methodStructure));
    CodeMethodExtractResult callerMethodExtractResult =
        new CodeMethodExtractResult(
            callerMethodName,
            EnumSet.of(MethodModifier.PUBLIC),
            callerMethodDescriptor.methodReturnType(),
            callerMethodDescriptor.argumentTypes(),
            1,
            1);
    OpenSourceRepoContent callerContent =
        OpenSourceRepoContent.of(
            callerOpenFile,
            callerStructure.classInfo(),
            List.of(callerMethodExtractResult),
            openSourceRepo);
    openSourceRepo.addAllContents(new ArrayList<>(List.of(callerContent, calleeContent)));
    openSourceRepoRepository.save(openSourceRepo);

    // when
    List<DeclaredMethod> declaredMethods =
        resolver.create(openSourceRepo.getId(), List.of(callerStructure, calleeStructure));

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
