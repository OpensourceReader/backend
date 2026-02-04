package com.opensourcereader.core.analysis.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodCallGraphServiceImpl implements MethodCallGraphService {

  private final MethodRepository methodRepository;

  @Transactional
  @Override
  public Method getCodeMethodById(Long codeMethodId) {
    methodRepository
        .findWithOutgoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);

    return methodRepository
        .findWithIngoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);
  }
}
