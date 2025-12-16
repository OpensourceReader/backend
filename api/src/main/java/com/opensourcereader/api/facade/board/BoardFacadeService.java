package com.opensourcereader.api.facade.board;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.api.controller.board.request.BoardGetRequest;
import com.opensourcereader.api.controller.board.response.BoardBaseResponse;
import com.opensourcereader.api.controller.board.response.BoardIssueResponse;
import com.opensourcereader.api.controller.board.response.BoardPreviewResponse;
import com.opensourcereader.api.controller.board.response.BoardPullResponse;
import com.opensourcereader.core.board.dto.IssueCommentDto;
import com.opensourcereader.core.board.dto.PullCommentDto;
import com.opensourcereader.core.board.dto.ReviewDto;
import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.board.entity.Review;
import com.opensourcereader.core.board.service.IssueCommentService;
import com.opensourcereader.core.board.service.IssueService;
import com.opensourcereader.core.board.service.LabelService;
import com.opensourcereader.core.board.service.PullCommentService;
import com.opensourcereader.core.board.service.PullService;
import com.opensourcereader.core.board.service.ReviewService;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardFacadeService {

  private final IssueService issueService;
  private final PullService pullService;

  private final IssueCommentService issueCommentService;
  private final PullCommentService pullCommentService;

  private final LabelService labelService;
  private final ReviewService reviewService;

  public List<BoardPreviewResponse> findAllByRepositoryId(BoardGetRequest request) {
    List<BoardPreviewResponse> responses = new ArrayList<>();
    Long repositoryId = request.repositoryId();

    List<Issue> issueEntities = issueService.findAllByRepositoryId(repositoryId, true);
    List<Pull> pullEntities = pullService.findAllByRepositoryId(repositoryId, true);

    for (Issue issue : issueEntities) {
      Long issueCommentCount = issueCommentService.countAllByIssueId(issue.getId());

      UserDto userDto = UserDto.from(issue.getUser());

      BoardPreviewResponse response = BoardPreviewResponse.of(issue, userDto, issueCommentCount);
      responses.add(response);
    }

    for (Pull pull : pullEntities) {
      Long pullId = pull.getId();
      List<Review> reviews = reviewService.findAllByPullId(pullId);
      Long pullCommentCount = reviewService.countAllByPullId(pullId);

      for (Review review : reviews) {
        pullCommentCount += pullCommentService.coundAllByReviewId(review.getId());
      }

      UserDto userDto = UserDto.from(pull.getUser());

      BoardPreviewResponse response = BoardPreviewResponse.of(pull, userDto, pullCommentCount);

      responses.add(response);
    }

    // TODO 리스트를 시간 내림차순으로 정렬해야함, label 붙여야 함
    return responses;
  }

  public BoardBaseResponse findByTagId(Long tagId, BoardGetRequest request) {
    Long repositoryId = request.repositoryId();
    boolean b1 = issueService.existedByTagId(repositoryId, tagId);
    boolean b2 = pullService.existedByTagId(repositoryId, tagId);

    if (b1) {
      Issue entity = issueService.findByTagId(repositoryId, tagId);
      UserDto userDto = UserDto.from(entity.getUser());

      List<IssueCommentDto> comments =
          issueCommentService.findAllByIssueId(entity.getId()).stream()
              .map(
                  comment -> {
                    UserDto author = UserDto.from(comment.getAuthor());
                    return IssueCommentDto.of(author, comment);
                  })
              .toList();

      BoardIssueResponse response = new BoardIssueResponse();
      response.setId(entity.getId());
      response.setTagId(entity.getTagId());
      response.setTitle(entity.getTitle());
      response.setBoardAuthor(userDto);
      response.setComments(comments);
      return response;
    } else if (b2) {
      Pull entity = pullService.findByTagId(repositoryId, tagId);
      UserDto userDto = UserDto.from(entity.getUser());

      List<ReviewDto> reviews = new ArrayList<>();
      List<PullCommentDto> comments = new ArrayList<>();

      List<Review> reviewEntities = reviewService.findAllByPullId(entity.getId());

      for (Review review : reviewEntities) {
        UserDto reviewAuthor = UserDto.from(review.getUser());
        ReviewDto reviewDto = ReviewDto.of(review, reviewAuthor);
        reviews.add(reviewDto);

        List<PullCommentDto> commentDtoList =
            pullCommentService.findAllByReviewId(review.getId()).stream()
                .map(
                    comment -> {
                      UserDto commentAuthor = UserDto.from(comment.getUser());

                      return PullCommentDto.of(comment, commentAuthor);
                    })
                .toList();

        comments.addAll(commentDtoList);
      }

      BoardPullResponse response = new BoardPullResponse();
      response.setId(entity.getId());
      response.setTagId(entity.getTagId());
      response.setTitle(entity.getTitle());
      response.setBoardAuthor(userDto);
      response.setReviews(reviews);
      response.setComment(comments);
      return response;
    }

    // TODO 오류를 던져야 함
    return null;
  }
}
