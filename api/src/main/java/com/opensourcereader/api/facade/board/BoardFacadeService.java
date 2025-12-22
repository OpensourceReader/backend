package com.opensourcereader.api.facade.board;

import java.util.ArrayList;
import java.util.Comparator;
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
import com.opensourcereader.core.board.exception.BoardNotFoundException;
import com.opensourcereader.core.board.service.IssueCommentService;
import com.opensourcereader.core.board.service.IssueService;
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

  private final ReviewService reviewService;

  public List<BoardPreviewResponse> findAllPreviewByRepositoryId(BoardGetRequest request) {
    List<BoardPreviewResponse> responses = new ArrayList<>();
    Long repositoryId = request.repositoryId();

    List<Issue> issueEntities = issueService.findAllByRepositoryId(repositoryId, true);
    List<Pull> pullEntities = pullService.findAllByRepositoryId(repositoryId, true);

    for (Issue issue : issueEntities) {
      Long issueCommentCount = issue.getCommentCount();

      UserDto userDto = UserDto.from(issue.getAuthor());

      BoardPreviewResponse response = BoardPreviewResponse.of(issue, userDto, issueCommentCount);
      responses.add(response);
    }

    for (Pull pull : pullEntities) {
      Long pullCommentCount = pull.getReviewCount() + pull.getCommentCount();

      UserDto userDto = UserDto.from(pull.getAuthor());

      BoardPreviewResponse response = BoardPreviewResponse.of(pull, userDto, pullCommentCount);

      responses.add(response);
    }

    return responses.stream()
        .sorted(Comparator.comparing(BoardPreviewResponse::createdAt).reversed())
        .toList();
  }

  // 지연 전략
  // issue랑 pull엔티티에 코멘트 수를 미리 저장해두기
  // 0이라면 조회하지 않기
  public BoardBaseResponse findByTagId(Long tagId, BoardGetRequest request) {
    Long repositoryId = request.repositoryId();
    boolean b1 = issueService.existedByTagId(repositoryId, tagId);
    boolean b2 = pullService.existedByTagId(repositoryId, tagId);

    if (b1) {
      Issue entity = issueService.findByTagId(repositoryId, tagId);
      UserDto userDto = UserDto.from(entity.getAuthor());
      List<IssueCommentDto> comments = new ArrayList<>();

      if (entity.getCommentCount() > 0) {
        comments =
            issueCommentService.findAllByIssueId(entity.getId()).stream()
                .map(
                    comment -> {
                      UserDto author = UserDto.from(comment.getAuthor());
                      return IssueCommentDto.of(author, comment);
                    })
                .toList();
      }

      BoardIssueResponse response = new BoardIssueResponse();
      response.setId(entity.getId());
      response.setTagId(entity.getTagId());
      response.setTitle(entity.getTitle());
      response.setBoardAuthor(userDto);
      response.setComments(comments);
      return response;
    } else if (b2) {
      Pull entity = pullService.findByTagId(repositoryId, tagId);
      UserDto userDto = UserDto.from(entity.getAuthor());

      List<ReviewDto> reviews = new ArrayList<>();
      List<PullCommentDto> comments = new ArrayList<>();

      // 리뷰가 있어야만 코멘트가 존재한다.
      if (entity.getReviewCount() > 0) {
        List<Review> reviewEntities = reviewService.findAllByPullId(entity.getId());

        for (Review review : reviewEntities) {
          UserDto reviewAuthor = UserDto.from(review.getAuthor());
          ReviewDto reviewDto = ReviewDto.of(review, reviewAuthor);
          reviews.add(reviewDto);

          // 리뷰만 존재할 수 있다.
          if (entity.getCommentCount() > 0) {
            List<PullCommentDto> commentDtoList =
                pullCommentService.findAllByReviewId(review.getId()).stream()
                    .map(
                        comment -> {
                          UserDto commentAuthor = UserDto.from(comment.getAuthor());

                          return PullCommentDto.of(comment, commentAuthor);
                        })
                    .toList();

            comments.addAll(commentDtoList);
          }
        }
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

    // 이슈랑 PR 둘 다 발견 못했으므로 404 오류
    throw new BoardNotFoundException();
  }
}
