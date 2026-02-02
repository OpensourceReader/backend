package com.opensourcereader.core.analysis.domain.service;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodCallResolver {

  private final OpenSourceRepoRepository openSourceRepoRepository;
  private final TypeRepository typeRepository;
  private final MethodRepository methodRepository;

  public List<Method> create(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    Set<Method> result = new HashSet<>();
    for (TypeStructureMeta typeStructureMeta : typeStructureMetas) {
      for (MethodStructure callerMethodStructure : typeStructureMeta.methods()) {
        MethodInfo callerMethodInfo = callerMethodStructure.methodInfo();
        Method callerMethod =
            methodRepository
                .findMethod(
                    repoId,
                    callerMethodInfo.typeName(),
                    MethodSignature.of(callerMethodInfo).methodSignature())
                .orElseThrow(IllegalArgumentException::new);

        for (MethodCallInfo calleeMethodInfo : callerMethodStructure.calleeMethods()) {
          Optional<Method> calleeMethod =
              methodRepository.findMethod(
                  repoId,
                  calleeMethodInfo.typeInternalName(),
                  MethodSignature.of(calleeMethodInfo).methodSignature());
          if (calleeMethod.isPresent()) {
            callerMethod.addOutgoingCall(calleeMethod.get());
            result.add(callerMethod);
            continue;
          }
          Optional<Type> calleeType =
              typeRepository.findByRepoAndTypeInternalName(
                  repoId, calleeMethodInfo.typeInternalName());
          if (calleeType.isEmpty()) {
            // 수정 바람
          }
        }
      }
    }
    return methodRepository.saveAll(result);
  }
}
