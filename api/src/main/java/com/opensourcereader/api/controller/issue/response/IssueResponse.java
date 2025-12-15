package com.opensourcereader.api.controller.issue.response;

import java.util.List;

import com.opensourcereader.core.issue.dto.IssueCommentDto;
import com.opensourcereader.core.issue.dto.IssueDto;
import com.opensourcereader.core.issue.dto.LabelDto;

public record IssueResponse(
    IssueDto issue, List<LabelDto> labels, List<IssueCommentDto> comments) {}
