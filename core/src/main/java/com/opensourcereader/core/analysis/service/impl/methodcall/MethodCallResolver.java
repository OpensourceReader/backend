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
import com.opensourcereader.core.analysis.entity.method.Method;
import com.opensourcereader.core.analysis.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodCallResolver {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final MethodRepository methodRepository;

  // 여기서 flatMap으로 한번 뽑아줄 수 있을듯, for문 하나 축소가능
  public List<Method> create(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    Set<Method> result = new HashSet<>();
    for (TypeStructureMeta typeStructureMeta : typeStructureMetas) {
      for (MethodStructure method : typeStructureMeta.methods()) {
        DeclaredMethodInfo methodInfo = method.methodInfo();
        String declaredMethodSignature = MethodSignature.of(methodInfo).methodSignature();
        Method declaredMethod =
            methodRepository
                .findCodeMethod(repoId, methodInfo.className(), declaredMethodSignature)
                .orElseThrow(IllegalArgumentException::new);

        for (MethodCallInfo calleeMethodInfo : method.calleeMethods()) {
          MethodSignature calleeMethodSignature = MethodSignature.of(calleeMethodInfo);
          Optional<Method> calleeMethod =
              methodRepository.findCodeMethod(
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
            Method internalInheritanceDeclared =
                Method.inheritedInternal(calleeMethodInfo, calleeMethodSignature);
            declaredMethod.addOutgoingCall(internalInheritanceDeclared);
            result.add(declaredMethod);
          }
          if (calleeDeclaredType.isEmpty()) {
            Method external = Method.external(calleeMethodInfo, calleeMethodSignature);
            declaredMethod.addOutgoingCall(external);
            result.add(declaredMethod);
          }
        }
      }
    }

    return methodRepository.saveAll(result);
  }
}
