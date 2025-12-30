package com.opensourcereader.core.analysis.service.impl.method;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.codemethod.AccessModifier;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.codemethod.MethodOrigin;
import com.opensourcereader.core.analysis.entity.codemethod.NonAccessModifier;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceContentRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class LocalOpenSourceRepoClassMethodServiceTest {

  @Autowired private LocalOpenSourceRepoClassMethodService service;
  @Autowired private CodeMethodRepository codeMethodRepository;
  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired private OpenSourceContentRepository openSourceContentRepository;

  @Transactional
  @Test
  @DisplayName(
      "서비스는 caller를 찾아 outgoing/ingoing(edge)을 동기화하고, missing callee/interface는 external로 연결한다")
  void createMethodCallGraph_connects_outgoing_and_ingoing_and_handles_external() {
    // given
    OpenSourceRepo repo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));
    String callerClassName = "t/Main";
    String callerDeclaredMethodName = "run";
    CodeMethod caller =
        saveInternalMethod(repo, callerClassName, callerDeclaredMethodName, List.of());
    DeclaredMethodInfo runInfo =
        DeclaredMethodInfo.of(
            callerClassName, /*access*/ 0x0001, callerDeclaredMethodName, "()V", null, null);

    String ingoingClassName = "t/I";
    String outgoingClassName = "t/Util";
    String outgoingMethodName = "help";
    MethodStructure outgoingMethodStructure =
        new MethodStructure(
            runInfo,
            List.of(MethodCallInfo.of(184, outgoingClassName, outgoingMethodName, "()V", false)));
    ClassStructure callerClassStructure =
        new ClassStructure(
            new ClassInfo(1, 1, callerClassName, "", "", List.of(ingoingClassName)),
            List.of(outgoingMethodStructure));

    // when
    List<CodeMethod> savedCaller =
        service.createMethodCallGraph(repo.getId(), List.of(callerClassStructure));

    // then
    Assertions.assertThat(savedCaller).isNotEmpty();
    // outgoing: Main.run -> Util.help (callee external)
    assertSoftly(
        softly -> {
          softly
              .assertThat(savedCaller.get(0).getOutgoingCalls())
              .extracting(
                  e -> e.getCaller().getMethodSignature().methodSignature(),
                  e -> e.getCallee().getMethodSignature().methodSignature(),
                  e -> e.getCallee().getOrigin())
              .contains(
                  Tuple.tuple(
                      CodeMethodSignature.of(callerDeclaredMethodName, List.of()).methodSignature(),
                      CodeMethodSignature.of(outgoingMethodName, List.of()).methodSignature(),
                      MethodOrigin.EXTERNAL));

          // ingoing: I.run -> Main.run (caller external(interface method))
          softly
              .assertThat(savedCaller.get(0).getIngoingCalls())
              .extracting(
                  e -> e.getCaller().getMethodSignature().methodSignature(),
                  e -> e.getCaller().getOrigin(),
                  e -> e.getCallee().getMethodSignature().methodSignature())
              .contains(
                  Tuple.tuple(
                      CodeMethodSignature.of(callerDeclaredMethodName, List.of()).methodSignature(),
                      MethodOrigin.EXTERNAL,
                      CodeMethodSignature.of(callerDeclaredMethodName, List.of())
                          .methodSignature()));
        });
  }

  private CodeMethod saveInternalMethod(
      OpenSourceRepo repo, String classInternalName, String methodName, List<String> paramTypes) {
    ClassInfo classInfo = new ClassInfo(1, 1, classInternalName, "", "", List.of());
    OpenSourceFileInfo fileInfo = new OpenSourceFileInfo("", "", "");

    OpenSourceRepoContent content =
        openSourceContentRepository.save(
            OpenSourceRepoContent.of(fileInfo, classInfo, List.of(), repo));

    CodeMethodExtractResult extract =
        new CodeMethodExtractResult(
            methodName,
            AccessModifier.PUBLIC,
            EnumSet.noneOf(NonAccessModifier.class),
            paramTypes,
            null,
            null);

    return codeMethodRepository.save(CodeMethod.internal(extract, content));
  }
}
