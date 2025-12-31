ALTER TABLE open_source_repo_content
    ADD COLUMN extension VARCHAR(200) NOT NULL DEFAULT '';

ALTER TABLE open_source_repo_content
    ADD COLUMN class_internal_name VARCHAR(512) NULL AFTER name;

ALTER TABLE code_method
    MODIFY open_source_repo_content_id BIGINT NULL;

ALTER TABLE code_method
    ADD class_internal_name varchar(300) NOT NULL;

ALTER TABLE code_method
    ADD COLUMN access_modifier VARCHAR(100);
ALTER TABLE code_method
    ADD COLUMN origin VARCHAR(300);


ALTER TABLE code_method_call_edge
    ADD COLUMN method_call_origin VARCHAR(100);

ALTER TABLE code_method
    MODIFY class_internal_name VARCHAR(300) NULL;
ALTER TABLE code_method
    ADD COLUMN return_type VARCHAR(300) NULL;