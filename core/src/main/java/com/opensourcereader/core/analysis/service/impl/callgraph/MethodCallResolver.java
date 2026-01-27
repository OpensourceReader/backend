package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodCallResolver {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final CodeMethodRepository codeMethodRepository;

  public List<DeclaredMethod> create(Long repoId, List<ClassStructure> classStructures) {
    List<DeclaredMethod> result = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      for (MethodStructure method : classStructure.methods()) {
        DeclaredMethodInfo declaredMethodInfo = method.declaredMethodInfo();
        String declaredMethodSignature =
            CodeMethodSignature.of(declaredMethodInfo).methodSignature();
        DeclaredMethod declaredMethod =
            codeMethodRepository
                .findCodeMethod(repoId, declaredMethodInfo.className(), declaredMethodSignature)
                .orElseThrow(IllegalArgumentException::new);
        for (MethodCallInfo methodCallInfo : method.calleeMethods()) {
          CodeMethodSignature calleeMethodSignature = CodeMethodSignature.of(methodCallInfo);
          Optional<DeclaredMethod> calleeMethod =
              codeMethodRepository.findCodeMethod(
                  repoId, methodCallInfo.className(), calleeMethodSignature.methodSignature());
          if (calleeMethod.isEmpty()) {
            Optional<DeclaredType> calleeDeclaredType =
                declaredTypeRepository.findByRepoAndTypeInternalName(
                    repoId, methodCallInfo.className());
            if (calleeDeclaredType.isPresent()) {
              DeclaredMethod internalInheritanceDeclared =
                  DeclaredMethod.internalInheritanceDeclared(methodCallInfo, calleeMethodSignature);
              declaredMethod.addOutgoingCall(internalInheritanceDeclared);
              result.add(declaredMethod);
            }
            if (calleeDeclaredType.isEmpty()) {
              DeclaredMethod external =
                  DeclaredMethod.external(methodCallInfo, calleeMethodSignature);
              declaredMethod.addOutgoingCall(external);
              result.add(declaredMethod);
            }
          }
          if (calleeMethod.isPresent()) {
            declaredMethod.addOutgoingCall(calleeMethod.get());
            result.add(declaredMethod);
          }
        }
      }
    }

    return codeMethodRepository.saveAll(result);
  }
}
