CREATE TABLE categories
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(80)           NOT NULL,
    slug VARCHAR(100)          NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE post_images
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    post_id    BIGINT                NOT NULL,
    image_path VARCHAR(300)          NOT NULL,
    sort_order INT                   NOT NULL,
    caption    VARCHAR(200)          NULL,
    CONSTRAINT pk_post_images PRIMARY KEY (id)
);

CREATE TABLE posts
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    title            VARCHAR(200)          NOT NULL,
    slug             VARCHAR(220)          NOT NULL,
    summary          VARCHAR(400)          NOT NULL,
    content          LONGTEXT              NOT NULL,
    cover_image_path VARCHAR(300)          NULL,
    youtube_video_id VARCHAR(50)           NULL,
    tags             VARCHAR(300)          NULL,
    category_id      BIGINT                NOT NULL,
    author_id        BIGINT                NOT NULL,
    view_count       BIGINT                NOT NULL,
    published        BIT(1)                NOT NULL,
    created_at       datetime              NOT NULL,
    updated_at       datetime              NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    full_name            VARCHAR(120)          NOT NULL,
    username             VARCHAR(60)           NOT NULL,
    email                VARCHAR(150)          NOT NULL,
    password             VARCHAR(255)          NOT NULL,
    `role`               VARCHAR(20)           NOT NULL,
    active               BIT(1)                NOT NULL,
    profile_picture_path VARCHAR(300)          NULL,
    bio                  TEXT                  NULL,
    created_at           datetime              NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_slug UNIQUE (slug);

ALTER TABLE posts
    ADD CONSTRAINT uc_posts_slug UNIQUE (slug);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE post_images
    ADD CONSTRAINT FK_POST_IMAGES_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);