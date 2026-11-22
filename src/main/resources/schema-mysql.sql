-- MySQL schema for SEO Rank Tracker.
-- JPA auto-creates tables via ddl-auto=update, but this file documents the intended schema.
-- Run manually: mysql -u root -p ranktracker < schema-mysql.sql

CREATE DATABASE IF NOT EXISTS ranktracker
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ranktracker;

CREATE TABLE IF NOT EXISTS websites (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain     VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255),
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_website_domain (domain)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS keywords (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    website_id BIGINT       NOT NULL,
    keyword    VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_keyword_website FOREIGN KEY (website_id) REFERENCES websites(id) ON DELETE CASCADE,
    CONSTRAINT uk_website_keyword UNIQUE (website_id, keyword),
    INDEX idx_keyword_website (website_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rankings (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT   NOT NULL,
    position   INT               DEFAULT NULL COMMENT '1-100, or NULL when not in top 100',
    search_url VARCHAR(2048),
    checked_on DATE     NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ranking_keyword FOREIGN KEY (keyword_id) REFERENCES keywords(id) ON DELETE CASCADE,
    CONSTRAINT uk_keyword_date UNIQUE (keyword_id, checked_on),
    INDEX idx_ranking_keyword (keyword_id),
    INDEX idx_ranking_date (checked_on)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alerts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id   BIGINT       NOT NULL,
    alert_type   VARCHAR(50)  NOT NULL,
    message      VARCHAR(500) NOT NULL,
    old_position INT,
    new_position INT,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_keyword FOREIGN KEY (keyword_id) REFERENCES keywords(id) ON DELETE CASCADE,
    INDEX idx_alert_keyword (keyword_id),
    INDEX idx_alert_read (is_read)
) ENGINE=InnoDB;
