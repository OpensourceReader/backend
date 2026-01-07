// package com.opensourcereader.api.facade.analysis;
//
// import static org.assertj.core.api.Assertions.assertThat;
//
// import java.util.EnumSet;
// import java.util.List;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.opensourcereader.api.dto.CodeMethodRequest;
// import com.opensourcereader.api.dto.CodeMethodResponse;
// import com.opensourcereader.api.dto.CodeMethodSummary;
// import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
// import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
// import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
// import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
// import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
// import com.opensourcereader.core.analysis.dto.callgraph.method.MethodDescriptor;
// import com.opensourcereader.core.analysis.entity.method.AccessModifier;
// import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
// import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
// import com.opensourcereader.core.analysis.entity.method.NonAccessModifier;
// import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
// import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
// import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
// import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
// import com.opensourcereader.core.analysis.repository.OpenSourceContentRepository;
// import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
//
// @ActiveProfiles("h2")
// @SpringBootTest
// class OpenSourceDeclaredMethodFacadeTest {
//  @Autowired private OpenSourceCodeMethodFacade openSourceCodeMethodFacade;
//  @Autowired private CodeMethodRepository codeMethodRepository;
//  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
//  @Autowired private OpenSourceContentRepository openSourceContentRepository;
//
//  @Transactional
//  @Test
//  @DisplayName("visible 정책에 따라 ingoing/outgoing이 필터링되어 응답으로 변환된다")
//  void getCodeMethodById_filters_by_policy() {
//    // given
//    OpenSourceRepo repo = openSourceRepoRepository.save(new OpenSourceRepo("clone-url"));
//
//    OpenSourceRepoContent aContent =
//        saveContent(
//            repo,
//            "t/A",
//            """
//            package t;
//            public class A {
//              void a() {}
//            }
//            """);
//    OpenSourceRepoContent bContent =
//        saveContent(
//            repo,
//            "t/B",
//            """
//            package t;
//            public class B {
//              public B() {}
//            }
//            """);
//    OpenSourceRepoContent mainContent =
//        saveContent(
//            repo,
//            "t/Main",
//            """
//            package t;
//            public class Main {
//              void run() {}
//            }
//            """);
//
//    // visible caller (ingoing에 남아야 함)
//    DeclaredMethod visibleCaller = saveInternalMethod(aContent, "a", "void", List.of(), 3, 3);
//    // hidden caller (<init> 이라 policy로 제외되어야 함)
//    DeclaredMethod hiddenCaller = saveInternalMethod(bContent, "<init>", "void", List.of(), 3, 3);
//    // external callee (outgoing에서 제외되어야 함)
//    DeclaredMethod externalCallee = saveExternalMethod("t/C", "c", "()V");
//
//    // target
//    DeclaredMethod target = saveInternalMethod(mainContent, "run", "void", List.of(), 3, 3);
//    target.updateAllCalls(
//        List.of(CodeMethodCallEdge.of(target, externalCallee)), // outgoing: external -> 제외 기대
//        List.of(
//            CodeMethodCallEdge.of(visibleCaller, target), // ngoing: a -> 포함 기대i
//            CodeMethodCallEdge.of(hiddenCaller, target) // ingoing: <init> -> 제외 기대
//            ));
//    codeMethodRepository.saveAll(List.of(visibleCaller, hiddenCaller, externalCallee, target));
//    CodeMethodRequest request = new CodeMethodRequest(target.getId(), false, false, false);
//
//    // when
//    CodeMethodResponse response = openSourceCodeMethodFacade.getCodeMethodById(request);
//
//    // then
//    assertThat(response.ingoing()).extracting(CodeMethodSummary::methodName).containsExactly("a");
//    assertThat(response.outgoing()).isEmpty(); // external은 제외
//  }
//
//  private OpenSourceRepoContent saveContent(
//      OpenSourceRepo repo, String classInternalName, String rawText) {
//    ClassInfo classInfo = new ClassInfo(1, 1, classInternalName, "", "java/lang/Object",
// List.of());
//    ClassStructure classStructure = new ClassStructure(classInfo, List.of());
//    OpenSourceFileInfo fileInfo = new OpenSourceFileInfo(classInternalName + ".java", "1",
// rawText);
//
//    return openSourceContentRepository.save(
//        OpenSourceRepoContent.of(fileInfo, classStructure, List.of(), repo));
//  }
//
//  private DeclaredMethod saveInternalMethod(
//      OpenSourceRepoContent content,
//      String methodName,
//      String returnType,
//      List<String> paramTypes,
//      Integer startLine,
//      Integer endLine) {
//    CodeMethodExtractResult extract =
//        new CodeMethodExtractResult(
//            methodName,
//            AccessModifier.PUBLIC,
//            EnumSet.noneOf(NonAccessModifier.class),
//            returnType,
//            paramTypes,
//            startLine,
//            endLine);
//
//    return codeMethodRepository.save(DeclaredMethod.internal(extract, content));
//  }
//
//  private DeclaredMethod saveExternalMethod(String className, String methodName, String
// descriptor) {
//    MethodCallInfo callInfo =
//        new MethodCallInfo(184, className, methodName, MethodDescriptor.from(descriptor), false);
//    CodeMethodSignature sig =
//        CodeMethodSignature.of(
//            methodName,
//            callInfo.descriptor().argumentTypes(),
//            callInfo.descriptor().methodReturnType());
//
//    DeclaredMethod external = DeclaredMethod.external(callInfo, sig);
//    return codeMethodRepository.save(external);
//  }
// }
