package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
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

  // 여기서 flatMap으로 한번 뽑아줄 수 있을듯, for문 하나 축소가능
  public List<DeclaredMethod> create(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    Set<DeclaredMethod> result = new HashSet<>();
    for (TypeStructureMeta typeStructureMeta : typeStructureMetas) {
      for (MethodStructure method : typeStructureMeta.methods()) {
        DeclaredMethodInfo methodInfo = method.methodInfo();
        String declaredMethodSignature = CodeMethodSignature.of(methodInfo).methodSignature();
        DeclaredMethod declaredMethod =
            codeMethodRepository
                .findCodeMethod(repoId, methodInfo.className(), declaredMethodSignature)
                .orElseThrow(IllegalArgumentException::new);

        for (MethodCallInfo calleeMethodInfo : method.calleeMethods()) {
          CodeMethodSignature calleeMethodSignature = CodeMethodSignature.of(calleeMethodInfo);
          Optional<DeclaredMethod> calleeMethod =
              codeMethodRepository.findCodeMethod(
                  repoId, calleeMethodInfo.className(), calleeMethodSignature.methodSignature());
          if (calleeMethod.isPresent()) {
            declaredMethod.addOutgoingCall(calleeMethod.get());
            result.add(declaredMethod);
            continue;
          }
          Optional<DeclaredType> calleeDeclaredType =
              declaredTypeRepository.findByRepoAndTypeInternalName(
                  repoId, calleeMethodInfo.className());
          if (calleeDeclaredType.isPresent()) {
            DeclaredMethod internalInheritanceDeclared =
                DeclaredMethod.internalInheritanceDeclared(calleeMethodInfo, calleeMethodSignature);
            declaredMethod.addOutgoingCall(internalInheritanceDeclared);
            result.add(declaredMethod);
          }
          if (calleeDeclaredType.isEmpty()) {
            DeclaredMethod external =
                DeclaredMethod.external(calleeMethodInfo, calleeMethodSignature);
            declaredMethod.addOutgoingCall(external);
            result.add(declaredMethod);
          }
        }
      }
    }

    return codeMethodRepository.saveAll(result);
  }
}
