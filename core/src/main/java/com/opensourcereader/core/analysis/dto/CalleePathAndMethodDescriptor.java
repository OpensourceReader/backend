package com.opensourcereader.core.analysis.dto;

import static com.opensourcereader.core.analysis.dto.ExtensionConstant.JAVA_EXTENSION;

public record CalleePathAndMethodDescriptor(String calleePath, String methodDescriptor) {

  public static CalleePathAndMethodDescriptor of(String calleeName, String methodDescriptor) {
    return new CalleePathAndMethodDescriptor(calleeName + JAVA_EXTENSION, methodDescriptor);
  }
}
