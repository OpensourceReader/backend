package com.opensourcereader.core.analysis.service.impl;

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
import com.opensourcereader.core.analysis.entity.method.AccessModifier;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;
import com.opensourcereader.core.analysis.entity.method.NonAccessModifier;
import com.opensourcereader.core.analysis.entity.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceContentRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.CodeMethodGraphQueryService;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class CodeMethodGraphQueryServiceTest {

  @Autowired CodeMethodGraphQueryService codeMethodGraphQueryService;
  @Autowired CodeMethodRepository codeMethodRepository;
  @Autowired OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired OpenSourceContentRepository openSourceContentRepository;

  @Transactional
  @Test
  @DisplayName("메서드 id로 조회하면 코드(rawText/라인) + outgoing/ingoing 그래프를 함께 가져온다")
  void getCodeMethodById() {
    // given
    OpenSourceRepo repo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));
    OpenSourceRepoContent targetContent =
        saveContent(
            repo,
            "t/Main",
            """
            package t;
            public class Main {
              void target() { Util.help(); }
            }
            """);
    OpenSourceRepoContent utilContent =
        saveContent(
            repo,
            "t/Util",
            """
            package t;
            public class Util {
              static void help() {}
            }
            """);
    OpenSourceRepoContent otherContent =
        saveContent(
            repo,
            "t/Other",
            """
            package t;
            public class Other {
              void run(Main m) { m.target(); }
            }
            """);

    CodeMethod target = saveInternalMethod(targetContent, "void", "target", List.of(), 3, 3);
    CodeMethod outgoingMethod = saveInternalMethod(utilContent, "void", "help", List.of(), 3, 3);
    CodeMethod ingoingMethod =
        saveInternalMethod(otherContent, "void", "run", List.of("t.Main"), 3, 3);
    target.updateAllCalls(
        List.of(CodeMethodCallEdge.of(target, outgoingMethod)),
        List.of(CodeMethodCallEdge.of(ingoingMethod, target)));
    codeMethodRepository.save(target);

    // when
    CodeMethod found = codeMethodGraphQueryService.getCodeMethodById(target.getId());

    // then (assert 너무 많지 않게 "핵심 3개"만)
    assertThat(found.getId()).isEqualTo(target.getId());
    assertThat(found.getOpenSourceRepoContent().getRawText()).contains("void target()");
    assertThat(found.getOutgoingCalls())
        .extracting(e -> e.getCallee().getId(), e -> e.getCallee().getMethodName())
        .containsExactlyInAnyOrder(Tuple.tuple(outgoingMethod.getId(), "help"));
    assertThat(found.getIngoingCalls())
        .extracting(e -> e.getCaller().getId(), e -> e.getCaller().getMethodName())
        .containsExactlyInAnyOrder(Tuple.tuple(ingoingMethod.getId(), "run"));
  }

  private OpenSourceRepoContent saveContent(
      OpenSourceRepo repo, String classInternalName, String rawText) {
    ClassInfo classInfo = new ClassInfo(1, 1, classInternalName, "", "java/lang/Object", List.of());
    ClassStructure classStructure = new ClassStructure(classInfo, List.of());
    OpenSourceFileInfo fileInfo = new OpenSourceFileInfo(classInternalName + ".java", "1", rawText);

    return openSourceContentRepository.save(
        OpenSourceRepoContent.of(fileInfo, classStructure, List.of(), repo));
  }

  private CodeMethod saveInternalMethod(
      OpenSourceRepoContent content,
      String returnType,
      String methodName,
      List<String> paramTypes,
      Integer startLine,
      Integer endLine) {
    CodeMethodExtractResult extract =
        new CodeMethodExtractResult(
            methodName,
            AccessModifier.PUBLIC,
            EnumSet.noneOf(NonAccessModifier.class),
            returnType,
            paramTypes,
            startLine,
            endLine);
    return codeMethodRepository.save(CodeMethod.internal(extract, content));
  }
}
