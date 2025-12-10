CREATE TABLE open_source_repo
(
    id bigint NOT NULL auto_increment,
    clone_url varchar(512) NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6),

    primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE open_source_repo_content
(
    id bigint NOT NULL auto_increment,
    opensource_repository_id bigint NOT NULL,
    path varchar(512) NOT NULL,
    name varchar(512) NOT NULL,
    content_type varchar(32) NOT NULL,
    raw_text longtext,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6),

    primary key (id),
    constraint fk_repo
        foreign key (opensource_repository_id)
        references open_source_repo(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
