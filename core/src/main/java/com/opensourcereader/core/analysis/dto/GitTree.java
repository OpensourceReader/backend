package com.opensourcereader.core.analysis.dto;

import java.util.List;

public record GitTree(
    String cloneUrl,
    List<GitTreeFileInfo> fileInfos
) {

}
