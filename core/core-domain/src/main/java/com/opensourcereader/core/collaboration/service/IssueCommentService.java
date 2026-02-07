package com.opensourcereader.core.collaboration.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.collaboration.dto.IssueCommentCommand;
import com.opensourcereader.core.collaboration.entity.IssueComment;
import com.opensourcereader.core.collaboration.repository.IssueCommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueCommentService {

  private final IssueCommentRepository issueCommentRepository;

  @Transactional
  public boolean saveIssueComments(List<IssueCommentCommand> commands) {
    List<IssueComment> entities = new ArrayList<>();
    for (IssueCommentCommand command : commands) {
      IssueComment issueComment = IssueComment.from(command);
      entities.add(issueComment);
    }
    issueCommentRepository.saveAll(entities);

    return true;
  }

  @Transactional(readOnly = true)
  public List<IssueComment> findAllByIssueId(Long issueId) {
    return issueCommentRepository.findAllByIssueId(issueId);
  }
}
