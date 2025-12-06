package com.opensourcereader.core.analysis.dto;

public record GitTreeFileDetail(
    String sha,
    String node_id,
    String size,
    String url,
    String content
) {

}