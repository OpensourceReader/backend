package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.entity.codedetail.MethodModifier;
import com.opensourcereader.core.analysis.repository.CodeMethodMetaDataRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.PathAndMethodSignature;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodFlowService {

  private final JavaAstExtractor javaAstExtractor;
  private final OpenSourceRepoRepository openSourceRepoRepository;
  private final CodeMethodMetaDataRepository codeMethodMetaDataRepository;

  @Transactional
  public OpenSourceRepo createMethodFlow(String cloneUrl) {
    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository
            .findByCloneUrl(cloneUrl)
            .orElseThrow(IllegalArgumentException::new);

    // 오픈 소스 코드 꺼내기
    for (OpenSourceRepoContent openSourceRepoContent : openSourceRepo.getContents()) {
      if (!openSourceRepoContent.getExtension().equals("java")) {
        continue;
      }
      // 코드의 메서드들의 메타 데이터에서 사용하는, 그니깐 밖으로 나가는 메서드들을 뽑음
      for (CodeMethodMetaData codeMethodMetaData : openSourceRepoContent.getCodeMethodMetaData()) {
        if (codeMethodMetaData.getMethodModifier().equals(MethodModifier.PRIVATE)) {
          continue;
        }
        List<PathAndMethodSignature> pathAndMethodSignatures =
            javaAstExtractor.extractOutgoingPathAndMethod(
                openSourceRepoContent.getRawText(), codeMethodMetaData.getMethodName());

        List<CodeMethodCallEdge> outgoingCodeMethodMetas = new ArrayList<>();
        for (PathAndMethodSignature pathAndMethodSignature : pathAndMethodSignatures) {
          //          System.out.println("직전path:"+calleePath);
          //          System.out.println("직전메서드:"+pathAndMethod.methodSignature());
          CodeMethodMetaData outgoingCodeMethodMetaData =
              codeMethodMetaDataRepository
                  .findByRepoContentPathAndMethodSignature(
                      pathAndMethodSignature.path(), pathAndMethodSignature.methodSignature())
                  .orElseThrow(IllegalArgumentException::new);

          outgoingCodeMethodMetas.add(
              new CodeMethodCallEdge(codeMethodMetaData, outgoingCodeMethodMetaData));
        }
        codeMethodMetaData.updateOutgoingCalls(outgoingCodeMethodMetas);
      }
      codeMethodMetaDataRepository.saveAll(openSourceRepoContent.getCodeMethodMetaData());
    }

    return openSourceRepo;
  }

  @Transactional
  public CodeMethodMetaData getMethodFlow(String path, String methodName) {
    return codeMethodMetaDataRepository
        .findByRepoContentPathAndMethodSignature(path, methodName)
        .orElseThrow(IllegalArgumentException::new);
  }
}
