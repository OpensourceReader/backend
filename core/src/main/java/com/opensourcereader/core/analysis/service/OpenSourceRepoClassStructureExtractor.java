package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;

public interface OpenSourceRepoClassStructureExtractor {
  List<ClassStructure> createClassStructures(
      Path savedLocalPath, String reference, String workingTreeDirName);
}
