package com.opensourcereader.core.analysis.dto.callgraph;

import java.nio.file.Path;

public record ClassBytecode(Path path, byte[] bytes) {}
