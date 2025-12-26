package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;

public interface OpenSourceRepoMethodCallAnalyzer {
  List<MethodCallsOfClass> createClassMethodCalls(
      Path savedLocalPath, String reference, String workingTreeDirName);
}
