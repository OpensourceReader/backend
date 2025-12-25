CREATE TABLE code_method
(
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    created_at                  TIMESTAMP(6) NOT NULL,
    updated_at                  TIMESTAMP(6) NOT NULL,
    open_source_repo_content_id BIGINT       NOT NULL,
    method_name                 VARCHAR(255) NULL,
    param_types                 JSON         NOT NULL,
    method_modifier             VARCHAR(50) NULL,
    method_signature            VARCHAR(512) NOT NULL,
    start_line                  INT NULL,
    end_line                    INT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_code_method_repo_content_method_name
        UNIQUE (open_source_repo_content_id, method_signature),
    CONSTRAINT fk_code_method_repo_content
        FOREIGN KEY (open_source_repo_content_id)
            REFERENCES open_source_repo_content (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE code_method_call_edge
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    caller_id  BIGINT       NOT NULL,
    callee_id  BIGINT       NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_edge_caller
        FOREIGN KEY (caller_id)
            REFERENCES code_method (id),
    CONSTRAINT fk_edge_callee
        FOREIGN KEY (callee_id)
            REFERENCES code_method (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;