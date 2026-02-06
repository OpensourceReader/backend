package com.opensourcereader.core.activity.dto;

import com.opensourcereader.core.activity.entity.Review;

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
public class PullCommentCommand extends CommentCommand {

  private Review review;

  private String diffHunk;

  private String path;
}
