package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethod;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodSignature;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoClassMethodService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoClassMethodService implements OpenSourceRepoClassMethodService {

  private final CodeMethodRepository codeMethodRepository;

  @Override
  @Transactional
  public List<CodeMethod> createMethodCallGraph(List<ClassStructure> classStructures) {
    List<CodeMethod> entireCodeMethods = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      Map<CodeMethodSignature, MethodStructure> methods =
          getByMethodSignature(classStructure.methods());

      List<CodeMethod> callers = resolveCallers(classStructure.methods());
      for (CodeMethod caller : callers) { // 두개 한번에 넣는 메서드 있어도됨
        caller.updateAllOutgoingCalls(
            createOutgoingCalls(
                caller, methods.get(caller.getMethodSignature()).calleeMethods())); // callee추출
        caller.updateAllIngoingCalls(
            createIngoingCalls(classStructure.classInfo().interfacePaths(), caller));
      }
      entireCodeMethods.addAll(codeMethodRepository.saveAll(callers));
    }
    return entireCodeMethods;
  }

  public Map<CodeMethodSignature, MethodStructure> getByMethodSignature(
      List<MethodStructure> methods) {
    return methods.stream()
        .collect(
            Collectors.toMap(
                cms ->
                    CodeMethodSignature.of(
                        cms.declaredMethodInfo().methodName(),
                        cms.declaredMethodInfo().methodDescriptor().argumentTypes()),
                cms -> cms));
  }

  // 클래스 메서드의 Caller를 가져옵니다, 수정필요
  private List<CodeMethod> resolveCallers(List<MethodStructure> methods) {
    return methods.stream()
        .map(
            methodStructure -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(methodStructure.declaredMethodInfo());
              return codeMethodRepository.findByRepoContentPathAndMethodSignature(
                  methodStructure.declaredMethodInfo().className(),
                  codeMethodSignature.methodSignature());
            })
        .flatMap(Optional::stream)
        .toList();
  }

  // graph 연결
  private List<CodeMethodCallEdge> createOutgoingCalls(
      CodeMethod caller, List<MethodCallInfo> calleeMethods) {
    return calleeMethods.stream()
        .map(
            call -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(call.methodName(), call.descriptor().argumentTypes());
              return codeMethodRepository.findByRepoContentPathAndMethodSignature(
                  call.className(), codeMethodSignature.methodSignature());
            })
        .flatMap(Optional::stream)
        .map(outgoing -> new CodeMethodCallEdge(caller, outgoing))
        .toList();
  }

  private List<CodeMethodCallEdge> createIngoingCalls(
      List<String> linkedInterfacePaths, CodeMethod caller) {
    return linkedInterfacePaths.stream()
        .map(
            path ->
                codeMethodRepository.findByRepoContentPathAndMethodSignature(
                    path, caller.getMethodSignature().methodSignature()))
        .flatMap(Optional::stream)
        .map(ingoing -> new CodeMethodCallEdge(ingoing, caller))
        .toList();
  }
}
