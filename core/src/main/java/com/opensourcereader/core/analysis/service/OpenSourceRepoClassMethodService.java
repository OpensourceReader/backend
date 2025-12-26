package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;

public interface OpenSourceRepoClassMethodService {

  void createMethodCallGraph(List<MethodCallsOfClass> methodCallsOfClasses);
}
