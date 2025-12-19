package com.opensourcereader.core.analysis;

public class OpenSourceRepoServiceTestFixture {

  public static final String OPEN_SOURCE_REPO_CONTEXT =
      """
          package com.opensourcereader.core.analysis.service.impl;

          import java.util.List;

          import org.springframework.stereotype.Service;

          import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
          import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
          import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
          import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
          import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
          import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
          import com.opensourcereader.core.analysis.service.GitRepositoryService;
          import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
          import jakarta.transaction.Transactional;
          import org.eclipse.jgit.lib.Repository;

          import lombok.RequiredArgsConstructor;

          @Service
          @RequiredArgsConstructor
          public class LocalOpenSourceRepoService implements OpenSourceRepoService {

            private final GitRepositoryService gitRepositoryService;
            private final OpenSourceRepoRepository opensourceRepoRepository;

            @Transactional
            @Override
            public OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference) {
              validateAlreadyExist(cloneUrl);

              OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUrl);
              List<GitTreeFileInfo> flatTree =
                  gitRepositoryService.getFlatTree(savedLocalPath, repoReference);

              Repository repo = gitRepositoryService.createRepositoryBuilder(savedLocalPath);
              for (GitTreeFileInfo fileInfo : flatTree) {
                String rawText = gitRepositoryService.getRawText(fileInfo.blobId(), repo);
                opensourceRepo.addContent(OpenSourceRepoContent.of(fileInfo, rawText, opensourceRepo));
              }

              return opensourceRepoRepository.save(opensourceRepo);
            }

            @Override
            public OpenSourceRepo getRepoById(Long repositoryId) {
              return opensourceRepoRepository
                  .findById(repositoryId)
                  .orElseThrow(OpenSourceRepoNotFoundException::new);
            }

            @Override
            public void deleteRepoById(Long repositoryId) {
              opensourceRepoRepository.deleteById(repositoryId);
            }

            private void validateAlreadyExist(String cloneUrl) {
              if (opensourceRepoRepository.existsByCloneUrl(cloneUrl)) {
                throw new OpenSourceRepoAlreadyExistException();
              }
            }
          }

          """;

  public static final String HIBERNATE_CONTEXT =
      """
          /*
           * SPDX-License-Identifier: Apache-2.0
           * Copyright Red Hat Inc. and Hibernate Authors
           */
          package org.hibernate.community.dialect;

          import org.hibernate.engine.spi.LoadQueryInfluencers;
          import org.hibernate.query.spi.QueryOptions;
          import org.hibernate.query.spi.QueryParameterBindings;
          import org.hibernate.query.sqm.internal.DomainParameterXref;
          import org.hibernate.query.sqm.sql.BaseSqmToSqlAstConverter;
          import org.hibernate.query.sqm.tree.SqmStatement;
          import org.hibernate.query.sqm.tree.expression.SqmExpression;
          import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
          import org.hibernate.sql.ast.spi.SqlAstCreationContext;
          import org.hibernate.sql.ast.tree.Statement;
          import org.hibernate.sql.ast.tree.expression.Expression;
          import org.hibernate.sql.ast.tree.expression.Literal;
          import org.hibernate.sql.ast.tree.from.NamedTableReference;
          import org.hibernate.sql.ast.tree.from.StandardTableGroup;
          import org.hibernate.sql.ast.tree.select.QuerySpec;

          /**
           * A SQM to SQL AST translator for Ingres.
           *
           * @author Christian Beikov
           */
          public class IngresSqmToSqlAstConverter<T extends Statement> extends BaseSqmToSqlAstConverter<T> {

              private boolean needsDummyTableGroup;

              public IngresSqmToSqlAstConverter(
                      SqmStatement<?> statement,
                      QueryOptions queryOptions,
                      DomainParameterXref domainParameterXref,
                      QueryParameterBindings domainParameterBindings,
                      LoadQueryInfluencers fetchInfluencers,
                      SqlAstCreationContext creationContext,
                      boolean deduplicateSelectionItems) {
                  super(
                          creationContext,
                          statement,
                          queryOptions,
                          fetchInfluencers,
                          domainParameterXref,
                          domainParameterBindings,
                          deduplicateSelectionItems
                  );
              }

              @Override
              public QuerySpec visitQuerySpec(SqmQuerySpec<?> sqmQuerySpec) {
                  final boolean needsDummy = this.needsDummyTableGroup;
                  this.needsDummyTableGroup = false;
                  try {
                      final QuerySpec querySpec = super.visitQuerySpec( sqmQuerySpec );
                      if ( this.needsDummyTableGroup ) {
                          querySpec.getFromClause().addRoot(
                                  new StandardTableGroup(
                                          true,
                                          null,
                                          null,
                                          null,
                                          new NamedTableReference( "(select 1)", "dummy_(x)" ),
                                          null,
                                          getCreationContext().getSessionFactory()
                                  )
                          );
                      }
                      return querySpec;
                  }
                  finally {
                      this.needsDummyTableGroup = needsDummy;
                  }
              }

              @Override
              protected Expression resolveGroupOrOrderByExpression(SqmExpression<?> groupByClauseExpression) {
                  final Expression expression = super.resolveGroupOrOrderByExpression( groupByClauseExpression );
                  if ( expression instanceof Literal ) {
                      // Note that SqlAstTranslator.renderPartitionItem depends on this
                      this.needsDummyTableGroup = true;
                  }
                  return expression;
              }
          }

          """;
}
