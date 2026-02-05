package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.TypeStructure;

public interface MethodCallGraphService {

  void create(List<Type> types, List<TypeStructure> typeStructures);

  Method getCodeMethodById(Long codeMethodId);
}
