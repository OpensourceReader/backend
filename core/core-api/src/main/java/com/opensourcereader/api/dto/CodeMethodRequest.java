package com.opensourcereader.api.dto;

public record CodeMethodRequest(
    Long codeMethodId, boolean includeInit, boolean includeExternal, boolean includeJdk) {}
