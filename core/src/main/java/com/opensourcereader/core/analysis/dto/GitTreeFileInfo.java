package com.opensourcereader.core.analysis.dto;

public record GitTreeFileInfo(
    String path,
    String mode,
    String type,
    Long size,
    String url
) {

}
