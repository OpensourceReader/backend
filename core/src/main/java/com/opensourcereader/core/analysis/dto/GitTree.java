package com.opensourcereader.core.analysis.dto;

import java.util.List;

public record GitTree(
    String url,
    List<GitTreeFileInfo> fileInfos
) {

}
