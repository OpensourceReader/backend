package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.entity.method.CodeMethod;

public interface CodeMethodGraphQueryService {
  CodeMethod getCodeMethodById(Long codeMethodId);
}
