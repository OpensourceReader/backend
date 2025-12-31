package com.opensourcereader.api.facade.analysis.viewpolicy;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.core.analysis.entity.codemethod.CodeMethod;
import com.opensourcereader.core.analysis.entity.codemethod.MethodOrigin;

@Component
public class CodeMethodViewPolicy {

  public boolean isVisible(CodeMethod method, CodeMethodRequest request) {
    if (method == null) {
      return false;
    }

    if (!request.includeInit() && "<init>".equals(method.getMethodName())) {
      return false;
    }

    if (!request.includeExternal() && method.getOrigin() == MethodOrigin.EXTERNAL) {
      return false;
    }

    if (!request.includeJdk()
        && method.getClassInternalName() != null
        && method.getClassInternalName().startsWith("java/")) {
      return false;
    }

    return true;
  }
}
