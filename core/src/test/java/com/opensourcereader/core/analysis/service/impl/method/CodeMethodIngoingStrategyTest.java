package com.opensourcereader.core.analysis.service.impl.method;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.codemethod.AccessModifier;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codemethod.MethodOrigin;
import com.opensourcereader.core.analysis.entity.codemethod.NonAccessModifier;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceContentRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class CodeMethodIngoingStrategyTest {
  @Autowired private CodeMethodIngoingStrategy strategy;
  @Autowired private CodeMethodRepository codeMethodRepository;
  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired private OpenSourceContentRepository openSourceContentRepository;

  @Transactional
  @Test
  @DisplayName(
      "interface method가 DB에 존재하면 해당 CodeMethod를 재사용해 ingoing edge(interface -> caller)를 만든다")
  void connectInterfaceToImpl_reuse_existing_interface_method() {
    // given
    OpenSourceRepo repo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));

    // impl caller: Main.run
    CodeMethod caller = getCodeMethod("t/Main", "run", repo);

    // interface method: I.run (caller와 동일 signature여야 함)
    CodeMethod ifaceMethod = seedMethodWithSignature("t/I", caller, repo);

    // when
    List<CodeMethodCallEdge> edges = strategy.getIngoing(repo.getId(), List.of("t/I"), caller);

    // then
    assertThat(edges)
        .hasSize(1)
        .extracting(
            e -> e.getCaller().getId(), // interface method id
            e -> e.getCallee().getId(), // impl caller id
            e -> e.getCaller().getOrigin(),
            e -> e.getCaller().getMethodSignature().methodSignature())
        .containsExactly(
            Tuple.tuple(
                ifaceMethod.getId(),
                caller.getId(),
                ifaceMethod.getOrigin(),
                caller.getMethodSignature().methodSignature()));
  }

  @Transactional
  @Test
  @DisplayName(
      "interface method가 DB에 없으면 external CodeMethod를 만들어 ingoing edge(interface -> caller)를 만든다(전략은 저장하지 않는다)")
  void getIngoing_create_external_when_missing() {
    // given
    OpenSourceRepo repo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));

    CodeMethod caller = getCodeMethod("t/Main", "run", repo);

    // when (DB에 t/I.run 저장 안 해둠)
    List<CodeMethodCallEdge> edges = strategy.getIngoing(repo.getId(), List.of("t/I"), caller);

    // then
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(edges).hasSize(1);

    CodeMethodCallEdge edge = edges.get(0);
    softly.assertThat(edge.getCallee().getId()).isEqualTo(caller.getId());
    softly.assertThat(edge.getCaller().getId()).isNull();
    softly.assertThat(edge.getCaller().getOrigin()).isEqualTo(MethodOrigin.EXTERNAL);

    softly.assertAll();
  }

  private CodeMethod getCodeMethod(String className, String methodName, OpenSourceRepo repo) {
    ClassInfo classInfo = new ClassInfo(1, 1, className, "", "", List.of());
    OpenSourceFileInfo fileInfo = new OpenSourceFileInfo("", "", "");

    OpenSourceRepoContent content =
        openSourceContentRepository.save(
            OpenSourceRepoContent.of(fileInfo, classInfo, List.of(), repo));

    CodeMethodExtractResult extract =
        new CodeMethodExtractResult(
            methodName,
            AccessModifier.PUBLIC,
            EnumSet.noneOf(NonAccessModifier.class),
            List.of(),
            null,
            null);

    return codeMethodRepository.save(CodeMethod.internal(extract, content));
  }

  /**
   * interfaceName에 "caller와 동일한 methodSignature"를 갖는 CodeMethod를 만들어 DB에 저장한다.
   * (MethodName/ParamTypes를 caller에서 가져와 signature를 동일하게 맞춤)
   */
  private CodeMethod seedMethodWithSignature(
      String interfaceName, CodeMethod caller, OpenSourceRepo repo) {
    // caller의 methodName/paramTypes로 signature 동일하게 만들기
    String methodName = caller.getMethodName();
    List<String> paramTypes = caller.getParamTypes();

    ClassInfo classInfo = new ClassInfo(1, 1, interfaceName, "", "", List.of());
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
