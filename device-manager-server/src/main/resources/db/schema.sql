-- Device Manager Schema
-- Reuses devicedb from Project 1; adds device_logs table

CREATE TABLE IF NOT EXISTS devices (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    ip_address  VARCHAR(45),
    location    VARCHAR(200),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Migration: add updated_at to existing tables from device-inventory-cli (Project 1).
-- PostgreSQL fills existing rows with DEFAULT NOW() — safe on 16.
ALTER TABLE devices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT NOW();

-- device_id uses ON DELETE SET NULL so deletion audit logs survive after the device is removed.
-- A NULL device_id means "this log belongs to a device that has since been deleted."
CREATE TABLE IF NOT EXISTS device_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id   UUID        REFERENCES devices(id) ON DELETE SET NULL,
    action      VARCHAR(50) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_device_logs_device_id ON device_logs(device_id);
CREATE INDEX IF NOT EXISTS idx_devices_status         ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_type           ON devices(type);
