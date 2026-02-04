package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.domain.entity.Method;

public interface MethodCallGraphService {

  Method getCodeMethodById(Long codeMethodId);
}
