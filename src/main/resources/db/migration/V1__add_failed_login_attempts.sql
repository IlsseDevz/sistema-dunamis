-- Migration: add failed_login_attempts column to users for login attempt tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0;
