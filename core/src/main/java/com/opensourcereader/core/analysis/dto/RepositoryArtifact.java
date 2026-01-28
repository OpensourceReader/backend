package com.opensourcereader.core.analysis.dto;

import java.nio.file.Path;
import java.util.List;

public record RepositoryArtifact(Path savedLocalRepoPath, List<TypeStructure> typeStructures) {}
