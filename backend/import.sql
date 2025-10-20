-- Apartment Management System Database Schema
-- This script initializes the database with essential tables only

-- Drop tables in reverse dependency order to avoid foreign key constraints
DROP TABLE IF EXISTS rental_requests;
DROP TABLE IF EXISTS maintenance_requests;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS leases;
DROP TABLE IF EXISTS tenants;
DROP TABLE IF EXISTS units;
DROP TABLE IF EXISTS users;

-- Create users table for authentication
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create units table (24 rooms: 12 per floor, 2 floors)
CREATE TABLE units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    floor INT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'AVAILABLE',
    type VARCHAR(50) NOT NULL,
    rent_amount DECIMAL(10,2) NOT NULL,
    size_sqm DECIMAL(8,2),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_floor (floor),
    INDEX idx_status (status)
);

-- Create tenants table
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    occupation VARCHAR(100),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    unit_id BIGINT,
    move_in_date DATE,
    lease_end_date DATE,
    monthly_rent DECIMAL(10,2),
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE SET NULL,
    INDEX idx_name (first_name, last_name),
    INDEX idx_phone (phone)
);

-- Create leases table
CREATE TABLE leases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    billing_cycle VARCHAR(255) NOT NULL DEFAULT 'MONTHLY',
    deposit_amount DECIMAL(10,2),
    status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_unit (unit_id),
    INDEX idx_dates (start_date, end_date)
);

-- Create payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lease_id BIGINT NOT NULL,
    payment_type VARCHAR(255) NOT NULL DEFAULT 'RENT',
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    payment_method VARCHAR(255) DEFAULT 'CASH',
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    receipt_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lease_id) REFERENCES leases(id) ON DELETE CASCADE,
    INDEX idx_lease (lease_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_payment_type (payment_type)
);

-- Create rental_requests table for booking system
CREATE TABLE rental_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    occupation VARCHAR(100),
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(20),
    lease_duration_months INT NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    request_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    approved_by_user_id BIGINT,
    approved_date DATETIME,
    rejection_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_unit (unit_id),
    INDEX idx_status (status),
    INDEX idx_request_date (request_date)
);

-- Create maintenance_requests table for tenant requests
CREATE TABLE maintenance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT,
    unit_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(255) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(255) NOT NULL DEFAULT 'OTHER',
    urgency VARCHAR(255) NOT NULL DEFAULT 'MEDIUM',
    preferred_time VARCHAR(100),
    status VARCHAR(255) NOT NULL DEFAULT 'SUBMITTED',
    assigned_to_user_id BIGINT,
    estimated_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    completion_notes TEXT,
    submitted_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_unit (unit_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_category (category)
);
-- Insert initial data

-- Insert 24 units (12 per floor, 2 floors)
INSERT IGNORE INTO units (room_number, floor, type, rent_amount, size_sqm) VALUES 
-- Floor 1
('101', 1, 'Standard', 8000.00, 25.0),
('102', 1, 'Standard', 8000.00, 25.0),
('103', 1, 'Standard', 8000.00, 25.0),
('104', 1, 'Standard', 8000.00, 25.0),
('105', 1, 'Deluxe', 10000.00, 30.0),
('106', 1, 'Deluxe', 10000.00, 30.0),
('107', 1, 'Standard', 8000.00, 25.0),
('108', 1, 'Standard', 8000.00, 25.0),
('109', 1, 'Standard', 8000.00, 25.0),
('110', 1, 'Standard', 8000.00, 25.0),
('111', 1, 'Deluxe', 10000.00, 30.0),
('112', 1, 'Deluxe', 10000.00, 30.0),
-- Floor 2
('201', 2, 'Standard', 8500.00, 25.0),
('202', 2, 'Standard', 8500.00, 25.0),
('203', 2, 'Standard', 8500.00, 25.0),
('204', 2, 'Standard', 8500.00, 25.0),
('205', 2, 'Premium', 12000.00, 35.0),
('206', 2, 'Premium', 12000.00, 35.0),
('207', 2, 'Standard', 8500.00, 25.0),
('208', 2, 'Standard', 8500.00, 25.0),
('209', 2, 'Standard', 8500.00, 25.0),
('210', 2, 'Standard', 8500.00, 25.0),
('211', 2, 'Premium', 12000.00, 35.0),
('212', 2, 'Premium', 12000.00, 35.0);

-- Insert admin user (password is 'admin123' - BCrypt encoded)
INSERT INTO users (username, password, email, role) VALUES 
('admin', '$2a$10$8K5bOPuqZMGP4LGH3Wr7jO.6eY0.sB1zJXV0YHKjbWmDqLg.8C7Bi', 'admin@apartment.com', 'ADMIN') ON DUPLICATE KEY UPDATE password=VALUES(password);

-- All tables are now ready for the apartment management system
-- Key features:
-- - User authentication (admin account only)
-- - 24 available apartment units across 2 floors  
-- - Rental request system for booking workflow
-- - Maintenance request system for tenant requests
-- - Payment and lease tracking (empty - to be populated by admin)
-- - Clean, optimized database structure