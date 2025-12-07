package com.opensourcereader.api.dto;

public record OpensourceRepoCreateRequest(
    String opensourceUri,
    String localPath,
    String repoReference
) {

}
