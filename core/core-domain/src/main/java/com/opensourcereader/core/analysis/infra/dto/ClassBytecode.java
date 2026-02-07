package com.opensourcereader.core.analysis.infra.dto;

import java.nio.file.Path;

public record ClassBytecode(Path path, byte[] bytes) {}
