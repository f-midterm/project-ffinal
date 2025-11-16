-- ============================================
-- Apartment Management System Database Schema
-- PRODUCTION-READY VERSION with Critical Fixes
-- ============================================
-- Changes:
-- 1. Fixed data redundancy in tenants table
-- 2. Removed UNIQUE constraint on leases.unit_id
-- 3. Added CHECK constraints for data validation
-- 4. Added missing indexes for performance
-- 5. Added audit columns (created_by, updated_by)
-- 6. Added soft delete support (deleted_at)
-- 7. Changed VARCHAR sizes for better compliance
-- 8. Changed TIMESTAMP to DATETIME (Year 2038 fix)
-- 9. Changed CASCADE to RESTRICT for safety
-- ============================================

USE apartment_db;

-- Drop tables in reverse dependency order to avoid foreign key constraints
DROP TABLE IF EXISTS maintenance_request_items;
DROP TABLE IF EXISTS maintenance_stocks;
DROP TABLE IF EXISTS rental_requests;
DROP TABLE IF EXISTS maintenance_requests;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS leases;
DROP TABLE IF EXISTS tenants;
DROP TABLE IF EXISTS units;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS apartment_settings;

-- ============================================
-- APARTMENT SETTINGS TABLE (Global settings for all units)
-- ============================================
CREATE TABLE apartment_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    description TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by_user_id BIGINT,
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,  -- Changed to 254 (RFC 5321 max)
    role ENUM('ADMIN', 'USER', 'VILLAGER') NOT NULL DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,  -- Changed from TIMESTAMP
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    INDEX idx_email (email),  -- Added for email lookups
    INDEX idx_username (username)  -- Added for login lookups
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- UNITS TABLE (24 rooms: 12 per floor, 2 floors)
-- ============================================
CREATE TABLE units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    floor INT NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    type VARCHAR(50) NOT NULL,
    rent_amount DECIMAL(10,2) NOT NULL,
    size_sqm DECIMAL(8,2),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    INDEX idx_floor (floor),
    INDEX idx_status (status),
    INDEX idx_room_number (room_number),
    CONSTRAINT chk_rent_amount CHECK (rent_amount > 0),  -- Validation
    CONSTRAINT chk_floor CHECK (floor > 0)  -- Validation
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- TENANTS TABLE (FIXED: Removed redundant columns)
-- ============================================
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,  -- Changed to NOT NULL
    email VARCHAR(254) NOT NULL,  -- Changed to 254 and NOT NULL
    occupation VARCHAR(100),
    emergency_contact VARCHAR(255) NOT NULL,  -- Changed to 255 and NOT NULL
    emergency_phone VARCHAR(20) NOT NULL,  -- Changed to NOT NULL
    -- REMOVED: unit_id (moved to leases only - fixes data redundancy)
    -- REMOVED: move_in_date (use leases.start_date)
    -- REMOVED: lease_end_date (use leases.end_date)
    -- REMOVED: monthly_rent (use leases.monthly_rent)
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    INDEX idx_name (first_name, last_name),
    INDEX idx_phone (phone),
    INDEX idx_email (email),  -- Added for email lookups
    INDEX idx_status (status),  -- Added for filtering
    UNIQUE KEY idx_email_unique (email)  -- Prevent duplicate emails
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- LEASES TABLE (FIXED: Removed UNIQUE on unit_id)
-- ============================================
CREATE TABLE leases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,  -- REMOVED UNIQUE to allow lease history
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    billing_cycle ENUM('MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL DEFAULT 'MONTHLY',
    deposit_amount DECIMAL(10,2) DEFAULT 0,
    status ENUM('ACTIVE', 'EXPIRED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    created_by_user_id BIGINT,  -- Audit trail
    updated_by_user_id BIGINT,  -- Audit trail
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,  -- Changed from CASCADE
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE RESTRICT,  -- Changed from CASCADE
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_unit (unit_id),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_status (status),
    INDEX idx_active_leases (status, end_date),  -- Composite index for active leases
    CONSTRAINT chk_lease_dates CHECK (end_date > start_date),  -- Date validation
    CONSTRAINT chk_monthly_rent CHECK (monthly_rent > 0),  -- Amount validation
    CONSTRAINT chk_deposit_amount CHECK (deposit_amount >= 0)  -- Amount validation
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- INVOICES TABLE (Master invoice with INV-YYYYMMDD-XXX format)
-- ============================================
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,  -- Format: INV-YYYYMMDD-XXX
    lease_id BIGINT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    invoice_type ENUM('MONTHLY_RENT', 'SECURITY_DEPOSIT', 'CLEANING_FEE', 'MAINTENANCE_FEE', 'CUSTOM') NOT NULL DEFAULT 'MONTHLY_RENT',
    status ENUM('PENDING', 'WAITING_VERIFICATION', 'PAID', 'OVERDUE', 'PARTIAL', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    slip_url VARCHAR(500),  -- Payment slip image URL
    slip_uploaded_at DATETIME,  -- When slip was uploaded
    verified_at DATETIME,  -- When payment was verified
    verified_by_user_id BIGINT,  -- Admin who verified
    verification_notes TEXT,  -- Admin notes on verification
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    created_by_user_id BIGINT,
    updated_by_user_id BIGINT,
    FOREIGN KEY (lease_id) REFERENCES leases(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (verified_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_lease (lease_id),
    INDEX idx_invoice_date (invoice_date),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_slip_uploaded (slip_uploaded_at),
    INDEX idx_verified (verified_at),
    CONSTRAINT chk_invoice_total_amount CHECK (total_amount > 0),
    CONSTRAINT chk_invoice_dates CHECK (due_date >= invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- PAYMENTS TABLE (Line items linked to invoice)
-- ============================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT,  -- NULL for standalone payments, NOT NULL for invoice line items
    lease_id BIGINT NOT NULL,
    payment_type ENUM('RENT', 'ELECTRICITY', 'WATER', 'MAINTENANCE', 'SECURITY_DEPOSIT', 'OTHER') NOT NULL DEFAULT 'RENT',
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'CHECK', 'ONLINE') DEFAULT 'CASH',
    status ENUM('PENDING', 'PAID', 'OVERDUE', 'PARTIAL') NOT NULL DEFAULT 'PENDING',
    receipt_number VARCHAR(50) UNIQUE,  -- Format: RENT-XXX, ELEC-XXX, WATER-XXX
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    created_by_user_id BIGINT,  -- Audit trail
    updated_by_user_id BIGINT,  -- Audit trail
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT,
    FOREIGN KEY (lease_id) REFERENCES leases(id) ON DELETE RESTRICT,  -- Changed from CASCADE
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_invoice (invoice_id),
    INDEX idx_lease (lease_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_payment_type (payment_type),
    INDEX idx_overdue_payments (status, due_date),  -- Composite index for overdue payments
    INDEX idx_receipt (receipt_number),  -- Added for receipt lookups
    CONSTRAINT chk_payment_amount CHECK (amount > 0)  -- Amount validation
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- RENTAL REQUESTS TABLE (For booking system)
-- ============================================
CREATE TABLE rental_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,  -- Links to authenticated user who created the request
    unit_id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL,  -- Changed to 254
    phone VARCHAR(20) NOT NULL,
    occupation VARCHAR(100),
    emergency_contact VARCHAR(255),  -- Changed to 255
    emergency_phone VARCHAR(20),
    lease_duration_months INT NOT NULL,
    -- REMOVED: monthly_rent (use units.rent_amount instead)
    -- REMOVED: total_amount (calculate in application: units.rent_amount * lease_duration_months)
    request_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    approved_by_user_id BIGINT,
    approved_date DATETIME,
    rejection_reason TEXT,
    rejection_acknowledged_at DATETIME NULL,  -- Tracks when user acknowledged rejection notification
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,  -- Link to requesting user
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE RESTRICT,  -- Changed from CASCADE
    FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),  -- For querying user's requests
    INDEX idx_unit (unit_id),
    INDEX idx_status (status),
    INDEX idx_request_date (request_date),
    INDEX idx_email (email),  -- Added for email lookups (backward compatibility)
    INDEX idx_user_status (user_id, status),  -- Composite index for user's active requests
    CONSTRAINT chk_lease_duration CHECK (lease_duration_months > 0)  -- Validation
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- MAINTENANCE REQUESTS TABLE
-- ============================================
CREATE TABLE maintenance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT,
    unit_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    category ENUM('PLUMBING', 'ELECTRICAL', 'HVAC', 'APPLIANCE', 'STRUCTURAL', 'CLEANING', 'OTHER') NOT NULL DEFAULT 'OTHER',
    urgency ENUM('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY') NOT NULL DEFAULT 'MEDIUM',
    preferred_time VARCHAR(100),
    status ENUM('NOT_SUBMITTED', 'SUBMITTED', 'WAITING_FOR_REPAIR', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SUBMITTED',
    assigned_to_user_id BIGINT,
    estimated_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    completion_notes TEXT,
    attachment_urls TEXT,
    submitted_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,  -- Soft delete support
    created_by_user_id BIGINT,  -- Audit trail
    updated_by_user_id BIGINT,  -- Audit trail
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    FOREIGN KEY (unit_id) REFERENCES units(id) ON DELETE SET NULL,  -- Changed to SET NULL since unit_id is now nullable
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_unit (unit_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_category (category),
    INDEX idx_active_maintenance (status, priority),  -- Composite index
    CONSTRAINT chk_estimated_cost CHECK (estimated_cost IS NULL OR estimated_cost >= 0),
    CONSTRAINT chk_actual_cost CHECK (actual_cost IS NULL OR actual_cost >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- MAINTENANCE STOCKS TABLE
-- ============================================
CREATE TABLE maintenance_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(200) NOT NULL,
    category ENUM('PLUMBING', 'ELECTRICAL', 'HVAC', 'APPLIANCE', 'STRUCTURAL', 'CLEANING', 'OTHER') NOT NULL DEFAULT 'OTHER',
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(50) NOT NULL,  -- Unit of measurement: ชิ้น, กล่อง, ม้วน, ลิตร
    unit_price DECIMAL(10,2),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_category (category),
    INDEX idx_item_name (item_name),
    CONSTRAINT chk_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_unit_price CHECK (unit_price IS NULL OR unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- MAINTENANCE REQUEST ITEMS TABLE (Junction table)
-- ============================================
CREATE TABLE maintenance_request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    maintenance_request_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    quantity_used INT NOT NULL,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maintenance_request_id) REFERENCES maintenance_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES maintenance_stocks(id) ON DELETE RESTRICT,
    INDEX idx_maintenance_request (maintenance_request_id),
    INDEX idx_stock (stock_id),
    CONSTRAINT chk_quantity_used CHECK (quantity_used > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- VIEWS FOR BACKWARDS COMPATIBILITY
-- ============================================

-- View to simulate the old tenants table structure with joined data
CREATE OR REPLACE VIEW v_tenants_with_lease AS
SELECT 
    t.id,
    t.first_name,
    t.last_name,
    t.phone,
    t.email,
    t.occupation,
    t.emergency_contact,
    t.emergency_phone,
    l.unit_id,
    l.start_date as move_in_date,
    l.end_date as lease_end_date,
    l.monthly_rent,
    l.status as lease_status,
    t.status as tenant_status,
    t.created_at,
    t.updated_at
FROM tenants t
LEFT JOIN leases l ON t.id = l.tenant_id AND l.status = 'ACTIVE';

-- View for rental requests with unit details
CREATE OR REPLACE VIEW v_rental_requests_with_unit AS
SELECT 
    rr.*,
    u.room_number,
    u.rent_amount as unit_rent,
    (u.rent_amount * rr.lease_duration_months) as calculated_total
FROM rental_requests rr
INNER JOIN units u ON rr.unit_id = u.id;

-- ============================================
-- TRIGGERS FOR DATA INTEGRITY
-- ============================================

-- Trigger to ensure only ONE active lease per unit
DELIMITER $$

CREATE TRIGGER trg_check_active_lease_insert
BEFORE INSERT ON leases
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    
    IF NEW.status = 'ACTIVE' THEN
        SELECT COUNT(*) INTO active_count
        FROM leases 
        WHERE unit_id = NEW.unit_id 
        AND status = 'ACTIVE'
        AND deleted_at IS NULL;
        
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Unit already has an active lease';
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_check_active_lease_update
BEFORE UPDATE ON leases
FOR EACH ROW
BEGIN
    DECLARE active_count INT;
    
    IF NEW.status = 'ACTIVE' AND OLD.status <> 'ACTIVE' THEN
        SELECT COUNT(*) INTO active_count
        FROM leases 
        WHERE unit_id = NEW.unit_id 
        AND status = 'ACTIVE'
        AND id <> NEW.id
        AND deleted_at IS NULL;
        
        IF active_count > 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Unit already has an active lease';
        END IF;
    END IF;
END$$

-- Trigger to auto-update unit status when lease status changes
CREATE TRIGGER trg_update_unit_status_insert
AFTER INSERT ON leases
FOR EACH ROW
BEGIN
    IF NEW.status = 'ACTIVE' THEN
        UPDATE units SET status = 'OCCUPIED' WHERE id = NEW.unit_id;
    END IF;
END$$

CREATE TRIGGER trg_update_unit_status_update
AFTER UPDATE ON leases
FOR EACH ROW
BEGIN
    IF NEW.status = 'ACTIVE' AND OLD.status <> 'ACTIVE' THEN
        UPDATE units SET status = 'OCCUPIED' WHERE id = NEW.unit_id;
    ELSEIF NEW.status <> 'ACTIVE' AND OLD.status = 'ACTIVE' THEN
        -- Check if there are no other active leases for this unit
        IF NOT EXISTS (SELECT 1 FROM leases WHERE unit_id = NEW.unit_id AND status = 'ACTIVE' AND id <> NEW.id) THEN
            UPDATE units SET status = 'AVAILABLE' WHERE id = NEW.unit_id;
        END IF;
    END IF;
END$$

-- Trigger to validate paid_date logic in payments
CREATE TRIGGER trg_validate_payment_paid_date_insert
BEFORE INSERT ON payments
FOR EACH ROW
BEGIN
    IF NEW.status = 'PAID' AND NEW.paid_date IS NULL THEN
        SET NEW.paid_date = CURRENT_DATE;
    ELSEIF NEW.status <> 'PAID' AND NEW.paid_date IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'paid_date must be NULL when status is not PAID';
    END IF;
END$$

CREATE TRIGGER trg_validate_payment_paid_date_update
BEFORE UPDATE ON payments
FOR EACH ROW
BEGIN
    IF NEW.status = 'PAID' AND NEW.paid_date IS NULL THEN
        SET NEW.paid_date = CURRENT_DATE;
    ELSEIF NEW.status <> 'PAID' AND NEW.paid_date IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'paid_date must be NULL when status is not PAID';
    END IF;
END$$

DELIMITER ;

-- ============================================
-- INSERT INITIAL DATA
-- ============================================

-- Insert apartment-wide settings
INSERT INTO apartment_settings (setting_key, setting_value, description) VALUES 
('ELECTRICITY_RATE', '4.00', 'Electricity rate per unit (Baht/Unit)'),
('WATER_RATE', '20.00', 'Water rate per unit (Baht/Unit)');

-- Insert 24 units (12 per floor, 2 floors)
INSERT INTO units (room_number, floor, type, rent_amount, size_sqm, description) VALUES 
-- Floor 1
('101', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('102', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('103', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('104', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('105', 1, 'Deluxe', 10000.00, 30.0, 'Deluxe room with premium amenities'),
('106', 1, 'Deluxe', 10000.00, 30.0, 'Deluxe room with premium amenities'),
('107', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('108', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('109', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('110', 1, 'Standard', 8000.00, 25.0, 'Standard room with basic amenities'),
('111', 1, 'Deluxe', 10000.00, 30.0, 'Deluxe room with premium amenities'),
('112', 1, 'Deluxe', 10000.00, 30.0, 'Deluxe room with premium amenities'),
-- Floor 2
('201', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('202', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('203', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('204', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('205', 2, 'Premium', 12000.00, 35.0, 'Premium room with luxury amenities'),
('206', 2, 'Premium', 12000.00, 35.0, 'Premium room with luxury amenities'),
('207', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('208', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('209', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('210', 2, 'Standard', 8500.00, 25.0, 'Standard room with basic amenities, higher floor'),
('211', 2, 'Premium', 12000.00, 35.0, 'Premium room with luxury amenities'),
('212', 2, 'Premium', 12000.00, 35.0, 'Premium room with luxury amenities');

-- Insert initial stock items for maintenance
INSERT INTO maintenance_stocks (item_name, category, quantity, unit, unit_price, description) VALUES
-- PLUMBING items
('Sink Faucet', 'PLUMBING', 10, 'piece', 350.00, 'Standard sink faucet'),
('Kitchen Faucet', 'PLUMBING', 8, 'piece', 450.00, 'Faucet for kitchen sink'),
('PVC Pipe 1/2 inch', 'PLUMBING', 50, 'meter', 25.00, 'PVC pipe 1/2 inch diameter'),
('PVC Pipe 3/4 inch', 'PLUMBING', 40, 'meter', 35.00, 'PVC pipe 3/4 inch diameter'),
('Pipe Connector', 'PLUMBING', 100, 'piece', 15.00, 'Various pipe connectors'),
('Rubber Gasket', 'PLUMBING', 50, 'piece', 10.00, 'Rubber gasket for leak prevention'),
('Toilet Bowl', 'PLUMBING', 3, 'piece', 2500.00, 'Budget toilet bowl'),
('Shower Head', 'PLUMBING', 8, 'piece', 280.00, 'Standard shower head'),

-- ELECTRICAL items
('LED Bulb 9W', 'ELECTRICAL', 100, 'bulb', 45.00, 'Energy saving LED bulb 9W'),
('LED Bulb 12W', 'ELECTRICAL', 80, 'bulb', 65.00, 'Energy saving LED bulb 12W'),
('LED Bulb 18W', 'ELECTRICAL', 60, 'bulb', 95.00, 'Energy saving LED bulb 18W'),
('Light Switch 1-way', 'ELECTRICAL', 50, 'piece', 35.00, '1-way light switch'),
('Light Switch 2-way', 'ELECTRICAL', 30, 'piece', 45.00, '2-way light switch'),
('3-pin Power Plug', 'ELECTRICAL', 40, 'piece', 25.00, '3-pin power plug'),
('2-socket Outlet', 'ELECTRICAL', 45, 'piece', 55.00, '2-socket power outlet'),
('THW Wire 2.5 sq.mm', 'ELECTRICAL', 200, 'meter', 12.00, 'THW wire 2.5 sq.mm'),
('Circuit Breaker 15A', 'ELECTRICAL', 15, 'piece', 180.00, '15 Amp circuit breaker'),
('Circuit Breaker 20A', 'ELECTRICAL', 12, 'piece', 210.00, '20 Amp circuit breaker'),

-- HVAC items
('Air Conditioner 12000 BTU', 'HVAC', 2, 'unit', 12500.00, 'Air conditioner 12000 BTU'),
('R32 Refrigerant', 'HVAC', 20, 'can', 850.00, 'R32 refrigerant for refill'),
('AC Filter', 'HVAC', 30, 'piece', 45.00, 'Air conditioner filter'),
('Exhaust Fan 6 inch', 'HVAC', 15, 'unit', 350.00, '6 inch exhaust fan'),
('Exhaust Fan 10 inch', 'HVAC', 10, 'unit', 550.00, '10 inch exhaust fan'),

-- APPLIANCE items
('Water Pump Motor 0.5 HP', 'APPLIANCE', 3, 'unit', 2800.00, '0.5 HP water pump motor'),
('Water Heater 3500W', 'APPLIANCE', 4, 'unit', 1850.00, 'Electric water heater 3500W'),
('Refrigerator 5 cu.ft', 'APPLIANCE', 1, 'unit', 5500.00, '5 cubic feet refrigerator'),
('Electric Iron', 'APPLIANCE', 5, 'unit', 350.00, 'Standard electric iron'),

-- STRUCTURAL items
('Interior Wall Paint', 'STRUCTURAL', 30, 'gallon', 450.00, 'White interior wall paint'),
('Cement', 'STRUCTURAL', 40, 'bag', 120.00, 'Cement for repair work'),
('Floor Tile 30x30 cm', 'STRUCTURAL', 100, 'piece', 35.00, 'Floor tile 30x30 cm'),
('Tile Grout', 'STRUCTURAL', 25, 'bag', 65.00, 'Grout for tile work'),
('PVC Bathroom Door', 'STRUCTURAL', 2, 'piece', 2200.00, 'PVC bathroom door with hinges'),
('Sliding Window', 'STRUCTURAL', 3, 'piece', 1800.00, 'Aluminum sliding window'),
('Wire Mesh Screen', 'STRUCTURAL', 50, 'meter', 45.00, 'Mosquito wire mesh screen'),

-- CLEANING items
('Bathroom Cleaner', 'CLEANING', 40, 'liter', 65.00, 'Bathroom cleaning solution'),
('Floor Cleaner', 'CLEANING', 50, 'liter', 55.00, 'Scented floor cleaner'),
('Toilet Brush', 'CLEANING', 30, 'piece', 35.00, 'Bathroom scrub brush'),
('Mop', 'CLEANING', 20, 'piece', 120.00, 'Mop with cloth'),
('Trash Can', 'CLEANING', 15, 'piece', 85.00, 'Medium plastic trash can'),

-- OTHER items
('Room Key', 'OTHER', 30, 'piece', 45.00, 'Room key with duplicate'),
('Door Handle', 'OTHER', 20, 'piece', 180.00, 'Standard door handle'),
('Door Hinge', 'OTHER', 40, 'piece', 25.00, '3 inch door hinge'),
('Nails', 'OTHER', 50, 'box', 35.00, 'Mixed size steel nails'),
('Screws', 'OTHER', 60, 'box', 40.00, 'Mixed size screws'),
('Pipe Tape', 'OTHER', 40, 'roll', 35.00, 'Leak prevention pipe tape'),
('Silicone Sealant', 'OTHER', 30, 'tube', 55.00, 'Waterproof silicone sealant');


-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Show table structures
SELECT 'Database schema created successfully!' as status;

-- Count records
SELECT 'apartment_settings' as table_name, COUNT(*) as record_count FROM apartment_settings
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'units', COUNT(*) FROM units
UNION ALL
SELECT 'tenants', COUNT(*) FROM tenants
UNION ALL
SELECT 'leases', COUNT(*) FROM leases
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'rental_requests', COUNT(*) FROM rental_requests
UNION ALL
SELECT 'maintenance_requests', COUNT(*) FROM maintenance_requests
UNION ALL
SELECT 'maintenance_stocks', COUNT(*) FROM maintenance_stocks
UNION ALL
SELECT 'maintenance_request_items', COUNT(*) FROM maintenance_request_items;

-- ============================================
-- PRODUCTION READY FEATURES:
-- ✅ Fixed data redundancy (removed duplicate columns from tenants)
-- ✅ Removed UNIQUE on leases.unit_id (allows lease history)
-- ✅ Added CHECK constraints (date validation, amount validation)
-- ✅ Added missing indexes (email, composite indexes)
-- ✅ Added audit columns (created_by, updated_by)
-- ✅ Added soft delete support (deleted_at)
-- ✅ Changed TIMESTAMP to DATETIME (Year 2038 fix)
-- ✅ Changed CASCADE to RESTRICT (prevents accidental data loss)
-- ✅ Added triggers for data integrity (one active lease per unit)
-- ✅ Added views for backwards compatibility
-- ✅ Improved VARCHAR sizes (email to 254, emergency_contact to 255)
-- ✅ Added UNIQUE constraint on receipt_number
-- ============================================
