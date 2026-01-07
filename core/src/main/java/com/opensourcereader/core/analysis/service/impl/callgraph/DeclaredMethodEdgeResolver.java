package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.DeclaredMethodEdges;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeclaredMethodEdgeResolver {

  private final CodeMethodRepository codeMethodRepository;

  public List<DeclaredMethodEdges> resolve(Long repoId, List<ClassStructure> classStructures) {
    List<DeclaredMethodEdges> result = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      for (MethodStructure methodStructure : classStructure.methods()) {
        DeclaredMethodInfo declaredMethodInfo = methodStructure.declaredMethodInfo();
        DeclaredMethod caller =
            codeMethodRepository
                .findByRepoIdAndTypeInternalNameAndMethodSignature(
                    repoId,
                    declaredMethodInfo.className(),
                    CodeMethodSignature.of(declaredMethodInfo).methodSignature())
                .orElse(null);
        if (caller == null) {
          continue;
        }

        // outgoing
        createOutgoingEdges(repoId, caller, methodStructure.calleeMethods());

        // ingoing
        createIngoingEdges(repoId, caller, classStructure.classInfo());
        //        result.add(new DeclaredMethodEdges());
      }
    }

    return result;
  }

  // 인터페이스/부모클래스
  private List<DeclaredMethod> createIngoingEdges(
      Long repoId, DeclaredMethod caller, ClassInfo classInfo) {
    // 한번 여기서 다 뽑고, 대기
    if (classInfo.superName() != null) {
      // 상속: 상속하는 클래스가 있고, 현재클래스에는 메서드가 없는데 사용되고 있고, 인터페이스에 해당하는 메서드가 아니고, 상속하는 곳에는 있을떄
    }
    for (String interfaceName : classInfo.interfaceNames()) {
      // 인터페이스 : 해당클래스에 메서드가 있고, 인터페이스에도 같은 메서드가 있으면 연결, 인터페이스에는 없으면 진행안함, 외부 인터페이스여서 찾아볼수가 없을떄도
      // 진행안함
    }

    return null;
  }

  // 단순하게, callee메서드 있는것듯은 연결하고, 없으면 external로 연결
  // 받는건, methodStruture 하나, outgoing만 넣어주던가
  private List<DeclaredMethod> createOutgoingEdges(
      Long repoId, DeclaredMethod caller, List<MethodCallInfo> calleeMethods) {
    for (MethodCallInfo calleeInfo : calleeMethods) {
      // 연결하는데 해당클래스로 메서드가 사용되고 있지만, 클래스 내부에는 없고 extend나 implement가 있다면, internal로 추가해도됨

    }
    return null;
  }
}
