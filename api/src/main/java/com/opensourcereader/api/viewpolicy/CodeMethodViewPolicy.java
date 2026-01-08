package com.opensourcereader.api.viewpolicy;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;

@Component
public class CodeMethodViewPolicy {

  public boolean isVisible(DeclaredMethod method, CodeMethodRequest request) {
    if (method == null) {
      return false;
    }

    if (!request.includeInit() && "<init>".equals(method.getMethodName())) {
      return false;
    }

    if (!request.includeExternal() && method.getOrigin() == MethodOrigin.EXTERNAL_RESOLVED) {
      return false;
    }

    if (!request.includeJdk()
        && method.getTypeInternalName() != null
        && method.getTypeInternalName().startsWith("java/")) {
      return false;
    }

    return true;
  }
}
