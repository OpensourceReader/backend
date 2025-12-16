package com.opensourcereader.core.analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.codedetail.CodeMethodMetaData;
import com.opensourcereader.core.analysis.entity.codedetail.MethodModifier;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenSourceRepoContentNameTest {

  @DisplayName("Path로 부터 이름을 추출합니다.")
  @Test
  void extractNameFromPath() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpenSourceRepoContentName opensourceRepoContentName = OpenSourceRepoContentName.from(path);

    // then
    assertThat(opensourceRepoContentName.getName()).isEqualTo("ci-report.yml");
  }

  @DisplayName("코드의 메서드의 메타 데이터들을 생성합니다.")
  @Test
  void createCodeMethodMetaData() {
    // given
    String rawText =
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
    String path = "IngresSqmToSqlAstConverter.java";

    // when
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo(path, ContentType.FILE, null), rawText, new OpenSourceRepo(""));

    // then
    Assertions.assertThat(content.getCodeMethodMetaData())
        .extracting(
            CodeMethodMetaData::getMethodName,
            CodeMethodMetaData::getMethodModifier,
            CodeMethodMetaData::getStartLine,
            CodeMethodMetaData::getEndLine)
        .containsExactlyElementsOf(
            List.of(
                Tuple.tuple("visitQuerySpec", MethodModifier.PUBLIC, 51, 75),
                Tuple.tuple("resolveGroupOrOrderByExpression", MethodModifier.PROTECTED, 77, 85)));
  }

  @DisplayName("자바파일의 메서드 추출시 자바파일이 아니면 추출하지 않습니다.")
  @Test
  void createCodeMethodMetaDataNotJavaFile() {
    // given
    String path = ".github/workflows/ci-report.yml";

    // when
    OpenSourceRepoContent content =
        OpenSourceRepoContent.of(
            new GitTreeFileInfo(path, ContentType.FILE, null), "", new OpenSourceRepo(""));

    // then
    Assertions.assertThat(content.getCodeMethodMetaData()).isEmpty();
  }
}
