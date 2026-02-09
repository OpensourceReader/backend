package com.opensourcereader.api.controller.board;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opensourcereader.api.controller.board.request.BoardGetRequest;
import com.opensourcereader.api.controller.board.response.BoardBaseResponse;
import com.opensourcereader.api.controller.board.response.BoardPreviewResponse;
import com.opensourcereader.api.facade.collaboration.BoardFacadeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/board")
@RequiredArgsConstructor
public class BoardController {

  private final BoardFacadeService boardFacadeService;

  @GetMapping
  public ResponseEntity<List<BoardPreviewResponse>> findAllPreview(
      @RequestBody BoardGetRequest request) {
    List<BoardPreviewResponse> responses = boardFacadeService.findAllPreviewByRepositoryId(request);

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{tagId}")
  public ResponseEntity<BoardBaseResponse> findByTagId(
      @PathVariable Integer tagId, @RequestBody BoardGetRequest request) {

    BoardBaseResponse response = boardFacadeService.findByTagId(tagId, request);

    return ResponseEntity.ok(response);
  }
}
