package com.opensourcereader.core.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.activity.entity.IssueComment;

public interface IssueCommentRepository extends JpaRepository<IssueComment, Long> {

  List<IssueComment> findAllByIssueId(Long issueId);
}
