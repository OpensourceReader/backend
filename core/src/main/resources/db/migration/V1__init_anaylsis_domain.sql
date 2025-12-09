CREATE TABLE open_source_repo
(
    id bigint not null auto_increment,
    clone_url varchar(512) not null,
        primary key (id)
) engine=InnoDB;


CREATE TABLE open_source_repo_content
(
    id bigint not null auto_increment,
    opensource_repository_id bigint not null,
    path varchar(512) not null,
    name varchar(512) not null,
    content_type varchar(32) not null,
    raw_text longtext,
    primary key (id),
    constraint fk_repo
        foreign key (opensource_repository_id)
        references open_source_repo(id)
) engine=InnoDB;
