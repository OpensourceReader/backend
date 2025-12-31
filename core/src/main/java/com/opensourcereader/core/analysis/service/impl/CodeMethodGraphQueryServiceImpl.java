package com.opensourcereader.core.analysis.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.method.CodeMethod;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodGraphQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodGraphQueryServiceImpl implements CodeMethodGraphQueryService {

  private final CodeMethodRepository codeMethodRepository;

  @Transactional
  @Override
  public CodeMethod getCodeMethodById(Long codeMethodId) {
    CodeMethod codeMethod =
        codeMethodRepository
            .findWithOutgoingGraphById(codeMethodId)
            .orElseThrow(IllegalArgumentException::new);
    codeMethodRepository
        .findWithIngoingGraphById(codeMethod.getId())
        .orElseThrow(IllegalArgumentException::new);

    return codeMethod;
  }
}
