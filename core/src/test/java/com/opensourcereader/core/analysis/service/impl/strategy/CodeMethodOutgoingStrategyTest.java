package com.opensourcereader.core.analysis.service.impl.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.codemethod.AccessModifier;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.codemethod.MethodOrigin;
import com.opensourcereader.core.analysis.entity.codemethod.NonAccessModifier;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceContentRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class CodeMethodOutgoingStrategyTest {

  @Autowired private CodeMethodOutgoingStrategy strategy;
  @Autowired private CodeMethodRepository codeMethodRepository;
  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired private OpenSourceContentRepository openSourceContentRepository;

  @Transactional
  @Test
  @DisplayName("callee가 DB에 존재하면 해당 CodeMethod를 재사용해 outgoing edge를 만든다")
  void getOutgoings_reuse_existing_callee() {
    // given
    OpenSourceRepo openSourceRepo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));
    CodeMethod caller = getCodeMethod("t/Main", "void", "run", openSourceRepo);
    CodeMethod callee = getCodeMethod("t/Util", "void", "help", openSourceRepo);
    MethodCallInfo call = MethodCallInfo.of(184, "t/Util", "help", "()V", false);

    // when
    List<CodeMethodCallEdge> edges =
        strategy.getOutgoings(openSourceRepo.getId(), caller, List.of(call));

    // then
    assertThat(edges)
        .hasSize(1)
        .extracting(
            e -> e.getCaller().getId(),
            e -> e.getCallee().getId(),
            e -> e.getCallee().getOrigin(),
            e -> e.getCallee().getMethodSignature().methodSignature())
        .containsExactly(
            Tuple.tuple(
                caller.getId(),
                callee.getId(),
                callee.getOrigin(),
                CodeMethodSignature.of(
                        "help",
                        call.descriptor().argumentTypes(),
                        call.descriptor().methodReturnType())
                    .methodSignature()));
  }

  @Transactional
  @Test
  @DisplayName("callee가 DB에 없으면 external CodeMethod를 만들어 outgoing edge를 만든다(전략은 저장하지 않는다)")
  void getOutgoings_create_external_when_missing() {
    // given
    OpenSourceRepo openSourceRepo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));
    CodeMethod caller = getCodeMethod("t/Main", "void", "run", openSourceRepo);
    MethodCallInfo missing =
        MethodCallInfo.of(184, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);

    // when
    List<CodeMethodCallEdge> edges =
        strategy.getOutgoings(openSourceRepo.getId(), caller, List.of(missing));

    // then
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(edges).hasSize(1);

    CodeMethodCallEdge edge = edges.get(0);
    softly.assertThat(edge.getCaller().getId()).isEqualTo(caller.getId());
    softly.assertThat(edge.getCallee().getId()).isNull();
    softly.assertThat(edge.getCallee().getOrigin()).isEqualTo(MethodOrigin.EXTERNAL);

    softly.assertAll();
  }

  private CodeMethod getCodeMethod(
      String className, String returnType, String methodName, OpenSourceRepo openSourceRepo) {
    ClassInfo classInfo = new ClassInfo(1, 1, className, "", "", List.of());
    ClassStructure classStructure = new ClassStructure(classInfo, List.of());
    OpenSourceFileInfo openSourceFileInfo = new OpenSourceFileInfo("", "", "");
    OpenSourceRepoContent openSourceRepoContent =
        openSourceContentRepository.save(
            OpenSourceRepoContent.of(
                openSourceFileInfo, classStructure, List.of(), openSourceRepo));
    CodeMethodExtractResult codeMethodExtractResult =
        new CodeMethodExtractResult(
            methodName,
            AccessModifier.PUBLIC,
            EnumSet.noneOf(NonAccessModifier.class),
            returnType,
            List.of(),
            null,
            null);
    return codeMethodRepository.save(
        CodeMethod.internal(codeMethodExtractResult, openSourceRepoContent));
  }
}
