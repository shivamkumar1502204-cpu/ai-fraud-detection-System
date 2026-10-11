-- ============================================================================
-- AI POWERED FRAUD DETECTION SYSTEM
-- Complete Database Schema & Seed Data for MySQL 8+
-- ============================================================================

CREATE DATABASE IF NOT EXISTS fraud_detection_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE fraud_detection_db;

-- ----------------------------------------------------------------------------
-- Drop tables in reverse dependency order
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS fraud_indicators;
DROP TABLE IF EXISTS fraud_analysis;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS users;

-- ----------------------------------------------------------------------------
-- Table: users
-- Stores system operators, compliance officers, and system administrators
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Table: transactions
-- Captures raw transactional telemetry, behavioral indicators, and payload
-- ----------------------------------------------------------------------------
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    sender_name VARCHAR(100) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    device VARCHAR(100) NOT NULL,
    transaction_frequency INT DEFAULT 1,
    new_device BOOLEAN DEFAULT FALSE,
    unusual_location BOOLEAN DEFAULT FALSE,
    suspicious_text TEXT,
    url VARCHAR(500),
    previous_amount DECIMAL(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tx_code (transaction_id),
    INDEX idx_tx_created (created_at),
    INDEX idx_tx_type (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Table: fraud_analysis
-- Stores the composite AI risk assessment output, score, level, and recommendation
-- ----------------------------------------------------------------------------
CREATE TABLE fraud_analysis (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    risk_score INT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    recommendation TEXT,
    reasons_json TEXT,
    analysis_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    INDEX idx_fa_score (risk_score),
    INDEX idx_fa_level (risk_level),
    INDEX idx_fa_decision (decision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Table: fraud_indicators
-- Fine-grained explainable AI indicators that contributed to the score
-- ----------------------------------------------------------------------------
CREATE TABLE fraud_indicators (
    id INT AUTO_INCREMENT PRIMARY KEY,
    analysis_id INT NOT NULL,
    indicator_type VARCHAR(50) NOT NULL,
    indicator_description TEXT NOT NULL,
    severity_weight DECIMAL(5, 2) DEFAULT 0.00,
    FOREIGN KEY (analysis_id) REFERENCES fraud_analysis(id) ON DELETE CASCADE,
    INDEX idx_ind_analysis (analysis_id),
    INDEX idx_ind_type (indicator_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Table: audit_logs
-- Immutable security audit log tracking user authentication and analyses
-- ----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (username),
    INDEX idx_audit_time (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- SEED DATA: Users
-- Default accounts:
-- 1. admin    / admin123 (Role: ADMIN)
-- 2. officer  / user123  (Role: USER)
-- ============================================================================
INSERT INTO users (username, password_hash, salt, role) VALUES
('admin', '9879a6c081970a685052bdaf0cba84347009c2f6f758c7a05257f5dcd6485ca8', 'a1b2c3d4e5f60718', 'ADMIN'),
('officer', '12aed06a58fce925441e7e05efec13299404503d081ac4cde908ece434e4f4ea', 'f8e7d6c5b4a39281', 'USER');

-- ============================================================================
-- SEED DATA: Safe, Suspicious, and Fraudulent Transactions
-- ============================================================================

-- 1. Safe Routine Transaction
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (1, 'TXN-SAFE-101', 'Alice Green', 'Whole Foods Market', 65.50, 'CARD_PAYMENT', 'Seattle, WA, USA', 'iPhone 14 (Known)', 1, FALSE, FALSE, 'Routine grocery store purchase at regular terminal', 'https://wholefoodsmarket.com/checkout', 58.20, NOW() - INTERVAL 2 DAY);

-- 2. Safe Small P2P Transfer
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (2, 'TXN-SAFE-102', 'David Miller', 'Sarah Miller', 120.00, 'P2P_TRANSFER', 'Chicago, IL, USA', 'Samsung Galaxy S22', 2, FALSE, FALSE, 'Happy birthday lunch reimbursement', NULL, 150.00, NOW() - INTERVAL 1 DAY);

-- 3. Suspicious Transaction: Velocity Spike + Higher Amount
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (3, 'TXN-SUSP-201', 'Robert Johnson', 'Apex Electronic Goods', 3750.00, 'ONLINE_PAYMENT', 'London, UK', 'MacBook Pro 16', 6, FALSE, TRUE, 'Please approve payment urgently so stock is reserved before sale ends', 'http://apex-deals-discount.com/order/99', 350.00, NOW() - INTERVAL 5 HOUR);

-- 4. Suspicious Transaction: New Device + International Transfer
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (4, 'TXN-SUSP-202', 'Elena Rostova', 'Global Currency FX', 4900.00, 'INTERNATIONAL_WIRE', 'Dubai, UAE', 'Windows 11 Laptop', 4, TRUE, TRUE, 'Account verification fee transfer for currency exchange clearance', 'https://secure-fx-portal.net/verify', 1200.00, NOW() - INTERVAL 2 HOUR);

-- 5. Fraudulent Transaction: High Amount + Phishing URL + Credential Thefts + Geo Anomaly
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (5, 'TXN-FRAUD-301', 'James Wilson', 'Anonymous Crypto Wallet 0x9B', 19800.00, 'CRYPTO_PURCHASE', 'Lagos, Nigeria', 'Linux Terminal v4', 12, TRUE, TRUE, 'URGENT: Your account is suspended! Send OTP and password immediately to unlock funds.', 'http://192.168.1.100/paypal-update-login/verify.php', 250.00, NOW() - INTERVAL 30 MINUTE);

-- 6. Fraudulent Transaction: Account Takeover + Social Engineering Prize Scam
INSERT INTO transactions (id, transaction_id, sender_name, receiver_name, amount, transaction_type, location, device, transaction_frequency, new_device, unusual_location, suspicious_text, url, previous_amount, created_at)
VALUES (6, 'TXN-FRAUD-302', 'Michael Chang', 'Quick Cash Express Payout', 14500.00, 'P2P_TRANSFER', 'Bucharest, Romania', 'Virtual Android Emulator', 9, TRUE, TRUE, 'Congratulations! You won the grand lottery. Send processing fee immediately to claim prize.', 'http://bit.ly/claim-grand-prize-2026', 100.00, NOW() - INTERVAL 10 MINUTE);

-- ============================================================================
-- SEED DATA: Fraud Analysis
-- ============================================================================
INSERT INTO fraud_analysis (id, transaction_id, risk_score, risk_level, decision, recommendation, reasons_json, analysis_time)
VALUES
(1, 1, 8, 'LOW', 'SAFE', 'Transaction cleared. Telemetry matches known baseline profile.', '["Amount within standard deviation", "Known trusted device", "HTTPS certified URL"]', NOW() - INTERVAL 2 DAY),
(2, 2, 14, 'LOW', 'SAFE', 'Transaction cleared. Typical peer-to-peer family activity.', '["Known location", "No urgent keywords", "Normal transaction frequency"]', NOW() - INTERVAL 1 DAY),
(3, 3, 58, 'MEDIUM', 'SUSPICIOUS', 'Flag transaction for step-up multi-factor authentication (MFA) and manual review.', '["Transaction amount $3750 exceeds baseline by 10.7x", "Location unusual for sender profile", "Insecure HTTP protocol detected"]', NOW() - INTERVAL 5 HOUR),
(4, 4, 66, 'HIGH', 'SUSPICIOUS', 'Hold transfer temporarily. Contact sender via verified out-of-band channel.', '["High value international wire", "Brand new untrusted device detected", "Elevated transaction velocity"]', NOW() - INTERVAL 2 HOUR),
(5, 5, 94, 'CRITICAL', 'FRAUDULENT', 'DO NOT PROCEED. Immediately trigger fraud investigation and lock compromised session.', '["Raw IP address URL detected", "Credential harvesting keywords detected (OTP, password)", "Critical amount spike ($19,800.00 vs $250.00 baseline)", "New untrusted device in foreign location"]', NOW() - INTERVAL 30 MINUTE),
(6, 6, 88, 'CRITICAL', 'FRAUDULENT', 'DO NOT PROCEED. High confidence social engineering lottery scam detected.', '["Known URL shortener detected (bit.ly)", "Social engineering reward keywords detected (lottery, won, claim prize)", "Device emulator detected"]', NOW() - INTERVAL 10 MINUTE);

-- ============================================================================
-- SEED DATA: Fraud Indicators
-- ============================================================================
INSERT INTO fraud_indicators (analysis_id, indicator_type, indicator_description, severity_weight) VALUES
(1, 'TELEMETRY_CHECK', 'Transaction conforms to registered cardholder behavioral patterns', 0.0),
(2, 'BENIGN_VELOCITY', 'Transaction frequency 2 is well within low-risk limits', 0.0),

(3, 'AMOUNT_ANOMALY', 'Transaction amount $3,750.00 is significantly above previous $350.00', 25.0),
(3, 'LOCATION_ANOMALY', 'Transaction initiated from London, UK (unusual location flag checked)', 18.0),
(3, 'URL_INSECURE', 'URL uses unencrypted HTTP protocol: http://apex-deals-discount.com/order/99', 15.0),

(4, 'DEVICE_ANOMALY', 'New hardware fingerprint detected: Windows 11 Laptop', 20.0),
(4, 'HIGH_RISK_TYPE', 'High-risk transaction category: INTERNATIONAL_WIRE', 22.0),
(4, 'VELOCITY_SPIKE', '4 transactions recorded within past hour', 14.0),

(5, 'URL_IP_BASED', 'URL utilizes raw IPv4 host pattern instead of legitimate domain: 192.168.1.100', 30.0),
(5, 'TEXT_CREDENTIAL_THEFT', 'Message demands sensitive security credentials: OTP, password', 28.0),
(5, 'AMOUNT_EXTREME_SPIKE', 'Severe amount deviation: $19,800.00 is 79.2x previous baseline ($250.00)', 26.0),
(5, 'GEO_DEVICE_CORRELATION', 'Untrusted Linux device originating from unusual foreign location (Lagos, Nigeria)', 20.0),

(6, 'URL_SHORTENER', 'High-risk obfuscated URL shortener domain detected: bit.ly', 25.0),
(6, 'TEXT_PRIZE_SCAM', 'Urgent social engineering prize lure keywords: congratulations, lottery, claim prize', 28.0),
(6, 'AMOUNT_ANOMALY', 'Disproportionate P2P transaction amount ($14,500.00)', 22.0);

-- ============================================================================
-- SEED DATA: Audit Logs
-- ============================================================================
INSERT INTO audit_logs (username, action, details, timestamp) VALUES
('SYSTEM', 'DATABASE_INITIALIZATION', 'Schema created and sample benchmark data populated successfully.', NOW() - INTERVAL 3 DAY),
('admin', 'USER_LOGIN', 'Administrator authenticated successfully from 127.0.0.1', NOW() - INTERVAL 2 DAY),
('officer', 'ANALYZE_TRANSACTION', 'Processed evaluation for TXN-SAFE-101. Risk Score: 8 (LOW)', NOW() - INTERVAL 2 DAY),
('officer', 'ANALYZE_TRANSACTION', 'Processed evaluation for TXN-SUSP-201. Risk Score: 58 (MEDIUM)', NOW() - INTERVAL 5 HOUR),
('admin', 'ANALYZE_TRANSACTION', 'Processed evaluation for TXN-FRAUD-301. Risk Score: 94 (CRITICAL)', NOW() - INTERVAL 30 MINUTE);
