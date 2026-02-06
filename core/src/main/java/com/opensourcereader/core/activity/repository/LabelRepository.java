package com.opensourcereader.core.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.opensourcereader.core.activity.entity.Issue;
import com.opensourcereader.core.activity.entity.Label;

@Deprecated
public interface LabelRepository extends JpaRepository<Label, Long> {

  List<Label> findAllByRepositoryId(Long repositoryId);

  List<Label> findAllByIssue(Issue issue);
}
