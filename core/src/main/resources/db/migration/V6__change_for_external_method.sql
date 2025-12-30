ALTER TABLE code_method
    MODIFY open_source_repo_content_id BIGINT NULL;

ALTER TABLE code_method
    ADD class_internal_name varchar(300) NOT NULL;