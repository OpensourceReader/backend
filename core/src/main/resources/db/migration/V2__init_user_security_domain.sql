CREATE TABLE users
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    provider_id BIGINT,
    role        VARCHAR(50),
    user_name   VARCHAR(100),
    login_name  VARCHAR(100),
    email       VARCHAR(255),
    password    VARCHAR(255),
    avatar_url  VARCHAR(500),
    disabled    BOOLEAN NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6),

    CONSTRAINT pk_users PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE refresh_token
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP(6) NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;