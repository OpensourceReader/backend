package com.opensourcereader.core.collaboration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.collaboration.entity.IssueComment;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

  List<IssueComment> findAllByIssueId(Long issueId);
}
