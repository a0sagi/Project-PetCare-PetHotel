CREATE DATABASE IF NOT EXISTS petcare_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE petcare_db;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id    INT           PRIMARY KEY,
    pet_name      VARCHAR(100)  NOT NULL,
    pet_type      VARCHAR(20)   NOT NULL,
    owner_name    VARCHAR(100)  NOT NULL,
    owner_phone   INT           NOT NULL,
    service_type  VARCHAR(20)   NOT NULL,
    duration_days INT           NOT NULL,
    status        VARCHAR(30)   NOT NULL,
    total_bill    DECIMAL(12,2) NOT NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
