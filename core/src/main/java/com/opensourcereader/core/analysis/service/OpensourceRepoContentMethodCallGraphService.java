package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;

public interface OpensourceRepoContentMethodCallGraphService {

  boolean createMethodCallGraph(Path savedLocalPath, String reference, String workingTreeDirName);
}
