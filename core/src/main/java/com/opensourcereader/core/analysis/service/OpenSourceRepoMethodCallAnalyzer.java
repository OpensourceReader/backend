package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;

public interface OpenSourceRepoMethodCallAnalyzer {
  List<ClassMethodCallResult> createClassMethodCalls(
      Path savedLocalPath, String reference, String workingTreeDirName);
}
