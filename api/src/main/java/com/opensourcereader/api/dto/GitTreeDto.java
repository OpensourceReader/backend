package com.opensourcereader.api.dto;

public record GitTreeDto(
    String path,
    String mode,
    String type,
    String sha,
    String size,
    String url
) {

}
