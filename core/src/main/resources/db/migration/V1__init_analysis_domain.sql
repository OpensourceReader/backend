CREATE TABLE open_source_repo
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    owner_name VARCHAR(512) NOT NULL,
    repo_name  VARCHAR(512) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    clone_url  VARCHAR(512) NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE open_source_repo_file
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    created_at         TIMESTAMP(6) NOT NULL,
    updated_at         TIMESTAMP(6),
    path               VARCHAR(512) NOT NULL,
    name               VARCHAR(512) NOT NULL,
    repo_file_type     VARCHAR(32)  NOT NULL,
    extension          VARCHAR(200) NOT NULL DEFAULT '',
    raw_text           LONGTEXT,
    origin             VARCHAR(100),
    opensource_repo_id BIGINT       NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_repo_file_repo
        FOREIGN KEY (opensource_repo_id)
            REFERENCES open_source_repo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE type
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    created_at              TIMESTAMP(6) NOT NULL,
    updated_at              TIMESTAMP(6),
    type_internal_name      VARCHAR(512) NOT NULL,
    type_kind               VARCHAR(255),
    super_type_id           BIGINT,
    origin                  VARCHAR(255) NOT NULL,
    opensource_repo_file_id BIGINT,
    opensource_repo_id      BIGINT       NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_type_file
        FOREIGN KEY (opensource_repo_file_id)
            REFERENCES open_source_repo_file (id),

    CONSTRAINT fk_type_repo
        FOREIGN KEY (opensource_repo_id)
            REFERENCES open_source_repo (id),

    CONSTRAINT fk_type_super
        FOREIGN KEY (super_type_id)
            REFERENCES type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE type_implementation
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6),
    implemented_type_id BIGINT       NOT NULL,
    interface_type_id   BIGINT       NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_impl_type
        FOREIGN KEY (implemented_type_id)
            REFERENCES type (id),

    CONSTRAINT fk_impl_interface
        FOREIGN KEY (interface_type_id)
            REFERENCES type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE method
(
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6),
    method_name      VARCHAR(255),
    return_type      VARCHAR(300),
    argument_types   JSON,
    method_modifiers JSON,
    method_signature VARCHAR(512) NOT NULL,
    start_line       INT,
    end_line         INT,
    origin           VARCHAR(300),
    type_id          BIGINT       NOT NULL,

    PRIMARY KEY (id),

    UNIQUE (type_id, method_signature),

    CONSTRAINT fk_method_type
        FOREIGN KEY (type_id)
            REFERENCES type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE method_call_edge
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    caller_id  BIGINT       NOT NULL,
    callee_id  BIGINT       NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_edge_caller
        FOREIGN KEY (caller_id)
            REFERENCES method (id),

    CONSTRAINT fk_edge_callee
        FOREIGN KEY (callee_id)
            REFERENCES method (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;