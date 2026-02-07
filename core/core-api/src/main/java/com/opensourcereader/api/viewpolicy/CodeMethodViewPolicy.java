package com.opensourcereader.api.viewpolicy;

import org.springframework.stereotype.Component;

import com.opensourcereader.api.dto.CodeMethodRequest;
import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.method.MethodOrigin;

@Component
public class CodeMethodViewPolicy {

  public boolean isVisible(Method method, CodeMethodRequest request) {
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
        && method.getType().getTypeInternalName() != null
        && method.getType().getTypeInternalName().startsWith("java/")) {
      return false;
    }

    return true;
  }
}
