-- =========================
-- RESET DATABASE
-- =========================
DROP DATABASE IF EXISTS quanlyhocvien;
CREATE DATABASE quanlyhocvien CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlyhocvien;

-- =========================
-- ROLES & ACCOUNT
-- =========================
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    date_of_birth DATE,
    phone_number VARCHAR(15),
    address VARCHAR(255),
    gender VARCHAR(10),
    avatar_path VARCHAR(100),
    role_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE admin (
    admin_id INT PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES account(account_id) ON DELETE CASCADE
);

CREATE TABLE teacher (
    teacher_id INT PRIMARY KEY,
    subject_specialization VARCHAR(100),
    qualification VARCHAR(100),
    hire_date DATE,
    FOREIGN KEY (teacher_id) REFERENCES account(account_id) ON DELETE CASCADE
);

CREATE TABLE student (
    student_id INT PRIMARY KEY,
    class_name VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES account(account_id) ON DELETE CASCADE
);

CREATE TABLE verification_token (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- =========================
-- COURSE STRUCTURE
-- =========================
CREATE TABLE course_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    icon_path VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE course (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category_id INT NOT NULL,
    thumbnail_path VARCHAR(255),
    price DECIMAL(10,2) DEFAULT 0,
    discount_price DECIMAL(10,2),
    duration INT,
    level VARCHAR(50),
    is_published BOOLEAN DEFAULT FALSE,
    published_at DATETIME,
    is_featured BOOLEAN DEFAULT FALSE,
    rating FLOAT DEFAULT 0,
    total_students INT DEFAULT 0,
    total_reviews INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES course_category(category_id)
);

CREATE TABLE module (
    module_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INT,
    is_free BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

CREATE TABLE lesson (
    lesson_id INT AUTO_INCREMENT PRIMARY KEY,
    module_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT,
    position INT,
    is_free BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (module_id) REFERENCES module(module_id) ON DELETE CASCADE
);

CREATE TABLE video (
    video_id INT AUTO_INCREMENT PRIMARY KEY,
    lesson_id INT NOT NULL,
    title VARCHAR(255),
    video_url VARCHAR(255) NOT NULL,
    thumbnail_path VARCHAR(255),
    duration INT,
    is_downloadable BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

CREATE TABLE lesson_attachment (
    attachment_id INT AUTO_INCREMENT PRIMARY KEY,
    lesson_id INT NOT NULL,
    title VARCHAR(255),
    file_path VARCHAR(255),
    file_type VARCHAR(50),
    file_size INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- =========================
-- ENROLLMENT & LEARNING
-- =========================
CREATE TABLE enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    student_id INT NOT NULL,
    enrollment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATETIME,
    payment_status VARCHAR(50),
    payment_amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    payment_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_student (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE progress (
    progress_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    lesson_id INT NOT NULL,
    video_position INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completion_date DATETIME,
    UNIQUE KEY uk_enroll_lesson (enrollment_id, lesson_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id)
);

CREATE TABLE review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    student_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_student (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- =========================
-- QUIZ SYSTEM
-- =========================
CREATE TABLE quiz (
    quiz_id INT AUTO_INCREMENT PRIMARY KEY,
    lesson_id INT NOT NULL,
    title VARCHAR(255),
    description TEXT,
    time_limit INT,
    passing_score INT,
    attempts_allowed INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

CREATE TABLE quiz_question (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT,
    question_type VARCHAR(50),
    points INT DEFAULT 1,
    position INT,
    FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE
);

CREATE TABLE quiz_answer (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    answer_text TEXT,
    is_correct BOOLEAN DEFAULT FALSE,
    position INT,
    FOREIGN KEY (question_id) REFERENCES quiz_question(question_id) ON DELETE CASCADE
);

CREATE TABLE quiz_attempt (
    attempt_id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    student_id INT NOT NULL,
    score INT,
    start_time DATETIME,
    submit_time DATETIME,
    time_spent INT,
    status VARCHAR(50),
    FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE student_answer (
    student_answer_id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_id INT,
    text_answer TEXT,
    is_correct BOOLEAN,
    points_earned INT DEFAULT 0,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempt(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES quiz_question(question_id),
    FOREIGN KEY (answer_id) REFERENCES quiz_answer(answer_id)
);

-- =========================
-- CERTIFICATE & NOTIFICATION
-- =========================
CREATE TABLE certificate (
    certificate_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT UNIQUE NOT NULL,
    certificate_code VARCHAR(100) UNIQUE,
    issue_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    certificate_path VARCHAR(255),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id)
);

CREATE TABLE notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    title VARCHAR(255),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    notification_type VARCHAR(50),
    reference_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);

-- =========================
-- CART
-- =========================
CREATE TABLE cart_item (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10,2),
    discount_price DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);
