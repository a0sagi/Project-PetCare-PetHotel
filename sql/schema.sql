CREATE DATABASE IF NOT EXISTS petcare_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE petcare_db;

-- ==========================================================
-- 1. TABEL MASTER LAYANAN (layanan)
-- ==========================================================
CREATE TABLE IF NOT EXISTS layanan (
    id_layanan     VARCHAR(20)   PRIMARY KEY,
    nama_layanan   VARCHAR(50)   NOT NULL,
    tarif_dasar    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    has_boarding   TINYINT(1)    NOT NULL DEFAULT 0,
    description    TEXT
) ENGINE=InnoDB;

-- ==========================================================
-- 2. TABEL DATA PEMILIK HEWAN (pemilik)
-- ==========================================================
CREATE TABLE IF NOT EXISTS pemilik (
    nomor_telepon      VARCHAR(20)   PRIMARY KEY,
    nama_pemilik       VARCHAR(100)  NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==========================================================
-- 3. TABEL DATA HEWAN PELIHARAAN (hewan)
-- ==========================================================
CREATE TABLE IF NOT EXISTS hewan (
    id_hewan           INT AUTO_INCREMENT PRIMARY KEY,
    nama_hewan         VARCHAR(100)  NOT NULL,
    jenis_hewan        VARCHAR(20)   NOT NULL,
    nomor_telepon      VARCHAR(20)   NOT NULL,
    catatan_khusus     TEXT,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hewan_pemilik FOREIGN KEY (nomor_telepon) 
        REFERENCES pemilik(nomor_telepon) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==========================================================
-- 4. TABEL TRANSAKSI PEMESANAN (bookings)
-- ==========================================================
CREATE TABLE IF NOT EXISTS bookings (
    id_booking         INT           PRIMARY KEY,
    nama_hewan         VARCHAR(100)  NOT NULL,
    jenis_hewan        VARCHAR(20)   NOT NULL,
    catatan_khusus     TEXT,
    nama_pemilik       VARCHAR(100)  NOT NULL,
    nomor_telepon      VARCHAR(20)   NOT NULL,
    jenis_layanan      VARCHAR(20)   NOT NULL,
    durasi_hari        INT           NOT NULL,
    status             VARCHAR(30)   NOT NULL,
    total_bill         DECIMAL(12,2) NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_pemilik FOREIGN KEY (nomor_telepon)
        REFERENCES pemilik(nomor_telepon) ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_layanan FOREIGN KEY (jenis_layanan)
        REFERENCES layanan(id_layanan) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==========================================================
-- 5. TABEL PELUNASAN / FAKTUR PEMBAYARAN (invoices)
-- ==========================================================
CREATE TABLE IF NOT EXISTS invoices (
    id_invoice         INT AUTO_INCREMENT PRIMARY KEY,
    id_booking         INT           NOT NULL UNIQUE,
    total_amount       DECIMAL(12,2) NOT NULL,
    status_pembayaran  VARCHAR(20)   NOT NULL DEFAULT 'PAID',
    issued_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_booking FOREIGN KEY (id_booking) 
        REFERENCES bookings(id_booking) ON DELETE CASCADE
) ENGINE=InnoDB;
