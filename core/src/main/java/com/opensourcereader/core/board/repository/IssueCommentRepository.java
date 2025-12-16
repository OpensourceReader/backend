package com.opensourcereader.core.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.board.entity.IssueComment;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

  List<IssueComment> findAllByIssueId(Long issueId);

  Long countAllByIssueId(Long issueId);
}
