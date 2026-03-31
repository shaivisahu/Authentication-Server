-- MySQL initialization script
-- Runs once when the container is first created

CREATE DATABASE IF NOT EXISTS authforge
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Flyway will handle all table creation via migrations
