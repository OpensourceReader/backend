package com.opensourcereader.core.analysis.dto;

public record ExternalMethodInfo(
    String typeInternalName, String methodName, MethodDescriptor descriptor) {}
