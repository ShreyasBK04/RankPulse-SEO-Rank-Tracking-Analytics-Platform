-- Sample seed data for SEO Rank Tracker MySQL database
-- Demonstrates realistic rank history, movements, and alerts

USE ranktracker;

-- 1. Websites
INSERT INTO websites (id, domain, name, created_at, updated_at) VALUES
(1, 'embarkx.com', 'EmbarkX Official', NOW() - INTERVAL 30 DAY, NOW()),
(2, 'spring.io', 'Spring Framework', NOW() - INTERVAL 20 DAY, NOW())
ON DUPLICATE KEY UPDATE domain=domain;

-- 2. Keywords for embarkx.com
INSERT INTO keywords (id, website_id, keyword, created_at) VALUES
(1, 1, 'spring boot tutorial', NOW() - INTERVAL 30 DAY),
(2, 1, 'java microservices course', NOW() - INTERVAL 30 DAY),
(3, 1, 'learn spring framework', NOW() - INTERVAL 30 DAY),
(4, 1, 'spring security guide', NOW() - INTERVAL 30 DAY),
(5, 1, 'spring boot rest api interview questions', NOW() - INTERVAL 30 DAY)
ON DUPLICATE KEY UPDATE keyword=keyword;

-- 3. Rankings history for 'spring boot tutorial' (climbing into top 10)
INSERT INTO rankings (keyword_id, position, search_url, checked_on, created_at) VALUES
(1, 16, 'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(1, 15, 'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(1, 13, 'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(1, 11, 'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(1, 10, 'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(1, 8,  'https://embarkx.com/courses/spring-boot', CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(1, 6,  'https://embarkx.com/courses/spring-boot', CURDATE(), NOW())
ON DUPLICATE KEY UPDATE position=VALUES(position);

-- Rankings history for 'java microservices course' (dropped slightly)
INSERT INTO rankings (keyword_id, position, search_url, checked_on, created_at) VALUES
(2, 9,  'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(2, 9,  'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, 10, 'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(2, 12, 'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(2, 14, 'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(2, 15, 'https://embarkx.com/courses/microservices', CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(2, 16, 'https://embarkx.com/courses/microservices', CURDATE(), NOW())
ON DUPLICATE KEY UPDATE position=VALUES(position);

-- Rankings history for 'learn spring framework' (stable top 5)
INSERT INTO rankings (keyword_id, position, search_url, checked_on, created_at) VALUES
(3, 4, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(3, 4, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(3, 3, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(3, 3, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(3, 3, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(3, 2, 'https://embarkx.com/tutorials/spring', CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(3, 2, 'https://embarkx.com/tutorials/spring', CURDATE(), NOW())
ON DUPLICATE KEY UPDATE position=VALUES(position);

-- Rankings history for 'spring security guide' (dropped out of top 100)
INSERT INTO rankings (keyword_id, position, search_url, checked_on, created_at) VALUES
(4, 45, 'https://embarkx.com/guides/security', CURDATE() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(4, 60, 'https://embarkx.com/guides/security', CURDATE() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(4, 85, 'https://embarkx.com/guides/security', CURDATE() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(4, NULL, NULL, CURDATE(), NOW())
ON DUPLICATE KEY UPDATE position=VALUES(position);

-- 4. Alerts
INSERT INTO alerts (id, keyword_id, alert_type, message, old_position, new_position, is_read, created_at) VALUES
(1, 1, 'ENTERED_TOP_10', "'spring boot tutorial' entered the top 10 (#11 → #10)", 11, 10, FALSE, NOW() - INTERVAL 2 DAY),
(2, 1, 'SIGNIFICANT_IMPROVEMENT', "'spring boot tutorial' improved 5 positions (#13 → #8)", 13, 8, FALSE, NOW() - INTERVAL 1 DAY),
(3, 2, 'SIGNIFICANT_DECLINE', "'java microservices course' declined 6 positions (#9 → #15)", 9, 15, FALSE, NOW() - INTERVAL 1 DAY),
(4, 4, 'DROPPED_FROM_TOP_100', "'spring security guide' dropped out of the top 100 (was #85)", 85, NULL, FALSE, NOW())
ON DUPLICATE KEY UPDATE message=VALUES(message);
