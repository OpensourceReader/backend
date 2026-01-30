package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClassSuperDispatcher {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final CodeMethodRepository codeMethodRepository;

  public List<DeclaredMethod> dispatchSupers(Long repoId) {
    List<DeclaredType> classes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);
    Set<DeclaredMethod> result = new HashSet<>();
    for (DeclaredType classType : classes) {
      result.addAll(dispatchSuperMethod(classType));
    }
    return codeMethodRepository.saveAll(result);
  }

  private List<DeclaredMethod> dispatchSuperMethod(DeclaredType childType) {
    List<DeclaredMethod> result = new ArrayList<>();
    DeclaredType superType = childType.getSuperType();
    if (superType == null) {
      return new ArrayList<>();
    }
    Map<CodeMethodSignature, DeclaredMethod> childMethods =
        childType.getDeclaredMethods().stream()
            .collect(Collectors.toMap(DeclaredMethod::getMethodSignature, it -> it));
    for (DeclaredMethod superMethod : superType.getDeclaredMethods()) {
      DeclaredMethod childMethodSameWithSuper = childMethods.get(superMethod.getMethodSignature());
      if (childMethodSameWithSuper == null) {
        childMethodSameWithSuper =
            DeclaredMethod.internalInheritanceDeclared(superMethod, childType);
        childType.updateMethod(childMethodSameWithSuper);
      }
      superMethod.addOutgoingCall(childMethodSameWithSuper);
      result.add(superMethod);
    }

    return codeMethodRepository.saveAll(result);
  }
}
