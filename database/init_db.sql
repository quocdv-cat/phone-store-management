-- ============================================================================
-- SCRIPT KHỞI TẠO CƠ SỞ DỮ LIỆU & DỮ LIỆU MẪU
-- DỰ ÁN: PHẦN MỀM QUẢN LÝ CỬA HÀNG ĐIỆN THOẠI DI ĐỘNG (mobile_store_db)
-- ============================================================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS mobile_store_db;
CREATE DATABASE mobile_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mobile_store_db;

-- 1. Bảng employees (Nhân viên)
CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100),
    address VARCHAR(255),
    position VARCHAR(50) NOT NULL DEFAULT 'Nhân viên bán hàng',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Bảng users (Tài khoản đăng nhập - Quan hệ 1-1 với employees)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. Bảng brands (Thương hiệu)
CREATE TABLE brands (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE,
    origin_country VARCHAR(100),
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 4. Bảng categories (Danh mục điện thoại)
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 5. Bảng products (Điện thoại)
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(30) NOT NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    brand_id INT NOT NULL,
    category_id INT NOT NULL,
    model VARCHAR(100),
    ram VARCHAR(20) NOT NULL,
    rom VARCHAR(20) NOT NULL,
    color VARCHAR(50) NOT NULL,
    import_price DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (import_price >= 0),
    selling_price DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (selling_price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    warranty_months INT NOT NULL DEFAULT 12 CHECK (warranty_months >= 0),
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(brand_id) ON DELETE RESTRICT,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 6. Bảng customers (Khách hàng)
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100),
    address VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. Bảng suppliers (Nhà cung cấp)
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100),
    address VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- 8. Bảng purchase_orders (Phiếu nhập hàng)
CREATE TABLE purchase_orders (
    purchase_id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_code VARCHAR(30) NOT NULL UNIQUE,
    supplier_id INT NOT NULL,
    employee_id INT NOT NULL,
    purchase_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    note VARCHAR(255),
    status ENUM('COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 9. Bảng purchase_details (Chi tiết phiếu nhập)
CREATE TABLE purchase_details (
    purchase_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    import_price DECIMAL(15, 2) NOT NULL CHECK (import_price >= 0),
    subtotal DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_pd_purchase FOREIGN KEY (purchase_id) REFERENCES purchase_orders(purchase_id) ON DELETE CASCADE,
    CONSTRAINT fk_pd_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 10. Bảng invoices (Hóa đơn bán hàng)
CREATE TABLE invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_code VARCHAR(30) NOT NULL UNIQUE,
    customer_id INT NOT NULL,
    employee_id INT NOT NULL,
    invoice_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    final_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (final_amount >= 0),
    note VARCHAR(255),
    status ENUM('COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT fk_inv_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 11. Bảng invoice_details (Chi tiết hóa đơn bán hàng)
CREATE TABLE invoice_details (
    invoice_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_id_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    CONSTRAINT fk_id_product FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ============================================================================
-- DỮ LIỆU MẪU (SAMPLE DATA)
-- ============================================================================

INSERT INTO employees (employee_code, full_name, phone, email, address, position, status) VALUES
('NV001', 'Nguyễn Quản Trị', '0901000001', 'admin@phonestore.vn', 'Quận 1, TP.HCM', 'Cửa hàng trưởng', 'ACTIVE'),
('NV002', 'Trần Thị Bán Hàng', '0901000002', 'staff1@phonestore.vn', 'Quận 3, TP.HCM', 'Nhân viên bán hàng', 'ACTIVE'),
('NV003', 'Lê Văn Thủ Kho', '0901000003', 'staff2@phonestore.vn', 'Quận Bình Thạnh, TP.HCM', 'Nhân viên kho', 'ACTIVE');

INSERT INTO users (employee_id, username, password_hash, role, status) VALUES
(1, 'admin', '123456', 'ADMIN', 'ACTIVE'),
(2, 'nhanvien1', '123456', 'STAFF', 'ACTIVE'),
(3, 'nhanvien2', '123456', 'STAFF', 'ACTIVE');

INSERT INTO brands (brand_name, origin_country, description) VALUES
('Apple', 'Mỹ', 'Dòng điện thoại iPhone chính hãng VN/A'),
('Samsung', 'Hàn Quốc', 'Điện thoại Samsung Galaxy S, Z, A series'),
('Xiaomi', 'Trung Quốc', 'Điện thoại Xiaomi & Redmi cấu hình cao'),
('OPPO', 'Trung Quốc', 'Điện thoại OPPO Reno & Find series');

INSERT INTO categories (category_name, description) VALUES
('Flagship Cao Cấp', 'Dòng máy đầu bảng hiệu năng và camera cao nhất'),
('Cận Cao Cấp & Tầm Trung', 'Dòng máy cân bằng hiệu năng và chi phí'),
('Điện Thoại Phổ Thông Giá Rẻ', 'Dòng máy nhu cầu cơ bản, pin lớn'),
('Điện Thoại Gập (Foldable)', 'Dòng smartphone màn hình gập công nghệ mới');

INSERT INTO products (product_code, product_name, brand_id, category_id, model, ram, rom, color, import_price, selling_price, stock_quantity, warranty_months) VALUES
('SP001', 'iPhone 16 Pro Max 256GB VN/A', 1, 1, 'A3296', '8GB', '256GB', 'Titan Tự Nhiên', 29500000, 33990000, 15, 12),
('SP002', 'iPhone 16 128GB VN/A', 1, 1, 'A3287', '8GB', '128GB', 'Xanh Lưu Ly', 19200000, 22490000, 20, 12),
('SP003', 'Samsung Galaxy S24 Ultra 5G', 2, 1, 'SM-S928B', '12GB', '256GB', 'Xám Titan', 24000000, 27990000, 10, 12),
('SP004', 'Samsung Galaxy Z Flip6 5G', 2, 4, 'SM-F741B', '12GB', '256GB', 'Xanh Mint', 20500000, 23990000, 4, 12),
('SP005', 'Samsung Galaxy A55 5G', 2, 2, 'SM-A556E', '8GB', '128GB', 'Tím Lilac', 7800000, 9490000, 25, 12),
('SP006', 'Xiaomi 14T Pro 5G', 3, 2, '2407FPN8EG', '12GB', '512GB', 'Đen Titan', 13500000, 16490000, 12, 18),
('SP007', 'Redmi Note 13 Pro 4G', 3, 3, '23117RA68G', '8GB', '256GB', 'Xanh Rừng', 5200000, 6490000, 3, 18),
('SP008', 'OPPO Reno12 5G', 4, 2, 'CPH2625', '12GB', '256GB', 'Bạc Vũ Trụ', 9800000, 11990000, 8, 12);

INSERT INTO customers (full_name, phone, email, address) VALUES
('Khách vãng lai', '0000000000', '', 'Mua trực tiếp tại cửa hàng'),
('Phạm Minh Tuấn', '0912345678', 'tuanpm@gmail.com', '123 Nguyễn Huệ, Q.1, TP.HCM'),
('Hoàng Thu Trang', '0987654321', 'tranght@gmail.com', '45 Lê Lợi, Q.1, TP.HCM'),
('Đỗ Đức Anh', '0933445566', 'anhdd@gmail.com', '88 Võ Văn Ngân, TP.Thủ Đức');

INSERT INTO suppliers (supplier_name, contact_person, phone, email, address) VALUES
('Công ty CP Phân phối Synnex FPT', 'Ngô Gia Bảo', '02839100100', 'contact@synnexfpt.com', 'Khu Công Nghệ Cao, TP.Thủ Đức'),
('Công ty TNHH Digiworld Việt Nam', 'Trịnh Thanh Hải', '02839290000', 'sales@dgw.com.vn', 'Quận 3, TP.HCM'),
('Công ty TNHH Viettel Commerce', 'Lý Hoàng Nam', '02462556789', 'b2b@viettelimex.vn', 'Quận Tân Bình, TP.HCM');

INSERT INTO purchase_orders (purchase_code, supplier_id, employee_id, purchase_date, total_amount, note) VALUES
('PN20261025-001', 1, 3, '2026-10-25 09:00:00', 295000000, 'Nhập lô iPhone 16 Pro Max đầu tháng'),
('PN20261028-001', 2, 3, '2026-10-28 14:30:00', 120000000, 'Nhập bổ sung Samsung S24 Ultra');

INSERT INTO purchase_details (purchase_id, product_id, quantity, import_price, subtotal) VALUES
(1, 1, 10, 29500000, 295000000),
(2, 3, 5, 24000000, 120000000);

INSERT INTO invoices (invoice_code, customer_id, employee_id, invoice_date, total_amount, discount_amount, final_amount, note) VALUES
('HD20261101-001', 2, 2, '2026-11-01 10:15:00', 33990000, 490000, 33500000, 'Tặng kèm ốp lưng'),
('HD20261102-001', 3, 2, '2026-11-02 16:40:00', 37480000, 0, 37480000, 'Khách thanh toán chuyển khoản');

INSERT INTO invoice_details (invoice_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 33990000, 33990000),
(2, 3, 1, 27990000, 27990000),
(2, 5, 1, 9490000, 9490000);
