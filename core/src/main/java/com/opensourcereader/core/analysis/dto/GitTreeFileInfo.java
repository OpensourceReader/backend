package com.opensourcereader.core.analysis.dto;

import com.opensourcereader.core.analysis.entity.ContentType;
import org.eclipse.jgit.lib.ObjectId;

public record GitTreeFileInfo(
    String path,
    ContentType type,
    ObjectId blobId
) {

}
