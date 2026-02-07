-- =========================
-- issues (SINGLE_TABLE 상속)
-- =========================
CREATE TABLE issues
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,

    dtype         VARCHAR(31)  NOT NULL,

    created_at    DATETIME(6),
    updated_at    DATETIME(6),

    provider_id   BIGINT,
    tag_id        INT,

    author_id     BIGINT       NOT NULL,
    repository_id BIGINT       NOT NULL,

    title         VARCHAR(255) NOT NULL,
    body          LONGTEXT,

    is_opened     BOOLEAN      NOT NULL,
    comment_count INT          NOT NULL,
    disabled      BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Pull 전용 필드 (SINGLE_TABLE 전략)
    review_count  INT,

    CONSTRAINT fk_issue_author
        FOREIGN KEY (author_id)
            REFERENCES users (id),

    CONSTRAINT fk_issue_repository
        FOREIGN KEY (repository_id)
            REFERENCES open_source_repo (id),

    INDEX         idx_issue_repository_id (repository_id),
    INDEX         idx_issue_author_id (author_id),
    INDEX         idx_issue_tag_id (tag_id)
) ENGINE=InnoDB;


-- =========================
-- issue_comments
-- =========================
CREATE TABLE issue_comments
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    created_at  DATETIME(6),
    updated_at  DATETIME(6),

    provider_id BIGINT,

    author_id   BIGINT NOT NULL,
    issue_id    BIGINT NOT NULL,

    body        LONGTEXT,

    CONSTRAINT fk_issue_comment_author
        FOREIGN KEY (author_id)
            REFERENCES users (id),

    CONSTRAINT fk_issue_comment_issue
        FOREIGN KEY (issue_id)
            REFERENCES issues (id),

    INDEX       idx_issue_comment_issue_id (issue_id)
) ENGINE=InnoDB;


-- =========================
-- reviews
-- =========================
CREATE TABLE reviews
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,

    provider_id  BIGINT       NOT NULL,
    submitted_at DATETIME(6),

    author_id    BIGINT       NOT NULL,
    pull_id      BIGINT       NOT NULL,

    body         VARCHAR(255) NOT NULL,

    CONSTRAINT fk_review_author
        FOREIGN KEY (author_id)
            REFERENCES users (id),

    CONSTRAINT fk_review_pull
        FOREIGN KEY (pull_id)
            REFERENCES issues (id),

    INDEX        idx_review_pull_id (pull_id)
) ENGINE=InnoDB;


-- =========================
-- pull_comments
-- =========================
CREATE TABLE pull_comments
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,

    created_at DATETIME(6),
    updated_at DATETIME(6),

    author_id  BIGINT       NOT NULL,
    review_id  BIGINT       NOT NULL,

    diff_hunk  LONGTEXT     NOT NULL,
    body       LONGTEXT     NOT NULL,
    path       VARCHAR(255) NOT NULL,

    CONSTRAINT fk_pull_comment_author
        FOREIGN KEY (author_id)
            REFERENCES users (id),

    CONSTRAINT fk_pull_comment_review
        FOREIGN KEY (review_id)
            REFERENCES reviews (id),

    INDEX      idx_pull_comment_review_id (review_id)
) ENGINE=InnoDB;


-- =========================
-- labels (deprecated지만 생성)
-- =========================
CREATE TABLE labels
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,

    repository_id BIGINT       NOT NULL,
    issue_id      BIGINT       NOT NULL,

    name          VARCHAR(255) NOT NULL,
    color_code    VARCHAR(255) NOT NULL,
    description   VARCHAR(255),

    CONSTRAINT fk_label_repository
        FOREIGN KEY (repository_id)
            REFERENCES open_source_repo (id),

    CONSTRAINT fk_label_issue
        FOREIGN KEY (issue_id)
            REFERENCES issues (id),

    INDEX         idx_label_repository_id (repository_id)
) ENGINE=InnoDB;