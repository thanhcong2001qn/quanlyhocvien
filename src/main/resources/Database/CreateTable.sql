CREATE TABLE roles
(
    role_id     INT PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tạo các role cơ bản
INSERT INTO roles (role_name, description)
VALUES ('admin', 'System administrator with full access'),
       ('teacher', 'Teacher role with academic privileges'),
       ('student', 'Student role with learning access');

-- Bảng account (bảng cha - tổng quát hóa)
CREATE TABLE account
(
    account_id    INT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50) UNIQUE  NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password      VARCHAR(255)        NOT NULL,
    full_name     VARCHAR(100)        NOT NULL,
    date_of_birth DATE,
    phone_number  VARCHAR(15),
    address       VARCHAR(255),
    gender        VARCHAR(10),
    avatar_path   varchar(100),
    role_id       INT                 NOT NULL,
    is_active     BOOLEAN  DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles (role_id)
);
-- Bảng admin (bảng con - chuyên biệt hóa)
CREATE TABLE admin
(
    admin_id INT PRIMARY KEY,
    --   department VARCHAR(50),
    FOREIGN KEY (admin_id) REFERENCES account (account_id) ON DELETE CASCADE
);

-- Bảng teacher (bảng con - chuyên biệt hóa)
CREATE TABLE teacher
(
    teacher_id             INT PRIMARY KEY,
    subject_specialization VARCHAR(100),
    qualification          VARCHAR(100),
    hire_date              DATE,
    FOREIGN KEY (teacher_id) REFERENCES account (account_id) ON DELETE CASCADE
);

-- Bảng student (bảng con - chuyên biệt hóa)
CREATE TABLE student
(
    student_id INT PRIMARY KEY,
    --   admission_number VARCHAR(20) UNIQUE,

    class      VARCHAR(20),
--     enrollment_date DATE,
--     graduation_year INT,
    FOREIGN KEY (student_id) REFERENCES account (account_id) ON DELETE CASCADE
);