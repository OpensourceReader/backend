package com.opensourcereader.api.controller.board.response;

import java.util.List;

import com.opensourcereader.core.board.dto.IssueCommentDto;
import com.opensourcereader.core.board.dto.IssueDto;
import com.opensourcereader.core.board.dto.LabelDto;

public record IssueResponse(
    IssueDto issue, List<LabelDto> labels, List<IssueCommentDto> comments) {}
