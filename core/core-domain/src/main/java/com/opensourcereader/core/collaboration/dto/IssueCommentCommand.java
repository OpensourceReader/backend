package com.opensourcereader.core.collaboration.dto;

import com.opensourcereader.core.collaboration.entity.Issue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class IssueCommentCommand extends CommentCommand {

  private Issue issue;
}
