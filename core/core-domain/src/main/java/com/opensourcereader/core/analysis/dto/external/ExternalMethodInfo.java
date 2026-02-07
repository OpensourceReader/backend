package com.opensourcereader.core.analysis.dto.external;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;

public record ExternalMethodInfo(
    String typeInternalName, String methodName, MethodDescriptor descriptor) {}
