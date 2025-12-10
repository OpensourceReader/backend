package com.opensourcereader.core.issue.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.issue.entity.Issue;
import com.opensourcereader.core.issue.entity.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {

  List<Label> findAllByRepositoryId(Long repositoryId);

  List<Label> findAllByIssue(Issue issue);
}
