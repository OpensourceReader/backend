ALTER TABLE open_source_repo_content
    ADD COLUMN class_internal_name VARCHAR(512) NULL AFTER name;