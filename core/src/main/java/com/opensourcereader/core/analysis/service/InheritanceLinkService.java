package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.TypeStructure;

public interface InheritanceLinkService {

  void resolve(List<Type> types, List<TypeStructure> typeStructures);
}
