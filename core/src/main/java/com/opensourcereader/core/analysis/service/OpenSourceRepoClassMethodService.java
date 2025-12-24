package com.opensourcereader.core.analysis.service;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;

public interface OpenSourceRepoClassMethodService {

  void createMethodCallGraph(List<ClassMethodCallResult> classMethodCallResults);
}
