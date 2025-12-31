package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodGraphCommandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodGraphCommandServiceImpl implements CodeMethodGraphCommandService {

  private final CodeMethodRepository codeMethodRepository;
  private final CodeMethodOutgoingEdgeFactory codeMethodOutgoingEdgeFactory;
  private final CodeMethodIngoingEdgeFactory codeMethodIngoingEdgeFactory;

  @Override
  @Transactional
  public List<CodeMethod> createMethodCallGraph(Long repoId, List<ClassStructure> classStructures) {
    List<CodeMethod> entireCodeMethods = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      Map<CodeMethodSignature, MethodStructure> methods = classStructure.methods().stream()
          .collect(
              Collectors.toMap(cms -> CodeMethodSignature.of(cms.declaredMethodInfo()),
                  cms -> cms));

      List<CodeMethod> callers = resolveCallers(repoId, classStructure.methods());
      callers.forEach(
          caller -> {
            List<CodeMethodCallEdge> outgoing =
                codeMethodOutgoingEdgeFactory.getOutgoings(
                    repoId, caller, methods.get(caller.getMethodSignature()).calleeMethods());
            List<CodeMethodCallEdge> ingoing =
                codeMethodIngoingEdgeFactory.getIngoing(
                    repoId, classStructure.classInfo().interfaceNames(), caller);
            caller.updateAllCalls(outgoing, ingoing);
          });
      entireCodeMethods.addAll(codeMethodRepository.saveAll(callers));
    }
    return entireCodeMethods;
  }

  private List<CodeMethod> resolveCallers(Long repoId, List<MethodStructure> methods) {
    return methods.stream()
        .map(method -> findCodeMethod(repoId, method))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<CodeMethod> findCodeMethod(Long repoId, MethodStructure methodStructure) {
    CodeMethodSignature codeMethodSignature =
        CodeMethodSignature.of(methodStructure.declaredMethodInfo());
    return codeMethodRepository.findByRepoIdAndClassInternalNameAndMethodSignature(
        repoId,
        methodStructure.declaredMethodInfo().className(),
        codeMethodSignature.methodSignature());
  }
}
