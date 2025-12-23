package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;

public interface OpensourceRepoContentMethodCallGraphService {

  void createMethodCallGraph(Path savedLocalPath, String reference, String workingTreeDirName);
}
