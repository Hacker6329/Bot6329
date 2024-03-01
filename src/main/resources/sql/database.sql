-- noinspection SqlNoDataSourceInspectionForFile
-- noinspection SqlDialectInspectionForFile
-- TABLES DECLARATION
CREATE TABLE IF NOT EXISTS key_parameters (
    param_key VARCHAR(32) NOT NULL PRIMARY KEY,
    param_value TEXT
);

CREATE TABLE IF NOT EXISTS guild_settings (
    guild_id VARCHAR(32) NOT NULL,
    setting_key VARCHAR(32) NOT NULL,
    setting_value TEXT,
    PRIMARY KEY(guild_id, setting_key)
);

CREATE TABLE IF NOT EXISTS guilds_blacklist (
    guild_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    PRIMARY KEY(guild_id, user_id)
);