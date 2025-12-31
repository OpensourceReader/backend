package com.opensourcereader.core.analysis.infra;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;

public interface ClassStructureExtractor {
  List<ClassStructure> createClassStructures(
      Path savedLocalPath, String reference, String workingTreeDirName);
}
