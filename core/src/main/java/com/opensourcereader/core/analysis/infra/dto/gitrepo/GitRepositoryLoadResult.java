package com.opensourcereader.core.analysis.infra.dto.gitrepo;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;

public record GitRepositoryLoadResult(Path savedLocalRepoPath, List<OpenSourceFileInfo> files) {}
