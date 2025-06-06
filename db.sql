DROP DATABASE IF EXISTS quanlyhocvien;
CREATE DATABASE quanlyhocvien;
USE quanlyhocvien;

-- Bảng roles
CREATE TABLE roles (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Tạo các role cơ bản
INSERT INTO roles (role_name, description) VALUES
('admin', 'System administrator with full access'),
('teacher', 'Teacher role with academic privileges'),
('student', 'Student role with learning access');

-- Bảng account (bảng cha - tổng quát hóa)
CREATE TABLE account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    date_of_birth DATE,
    phone_number VARCHAR(15),
    address VARCHAR(255),
    gender VARCHAR(10),
    avatar_path VARCHAR(100),
    role_id BIGINT NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Bảng admin
CREATE TABLE admin (
    admin_id BIGINT PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Bảng teacher
CREATE TABLE teacher (
    teacher_id BIGINT PRIMARY KEY,
    subject_specialization VARCHAR(100),
    qualification VARCHAR(100),
    hire_date DATE,
    FOREIGN KEY (teacher_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Bảng student
CREATE TABLE student (
    student_id BIGINT PRIMARY KEY,
    class_name VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Verification token
CREATE TABLE verification_token (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Danh mục khóa học
CREATE TABLE course_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    icon_path VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Khóa học
CREATE TABLE course (
    course_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    thumbnail_path VARCHAR(255),
    price DECIMAL(10, 2) DEFAULT 0,
    discount_price DECIMAL(10, 2),
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

-- Module
CREATE TABLE module (
    module_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INT,
    is_free BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

-- Lesson
CREATE TABLE lesson (
    lesson_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT,
    position INT,
    is_free BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (module_id) REFERENCES module(module_id) ON DELETE CASCADE
);

-- Video
CREATE TABLE video (
    video_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    video_url VARCHAR(255) NOT NULL,
    thumbnail_path VARCHAR(255),
    duration INT,
    is_downloadable BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- Tài liệu đính kèm
CREATE TABLE lesson_attachment (
    attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- Enrollment
CREATE TABLE enrollment (
    enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrollment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATETIME,
    payment_status VARCHAR(50) DEFAULT 'pending',
    payment_amount DECIMAL(10, 2),
    payment_method VARCHAR(50),
    transaction_id VARCHAR(100),
    payment_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Tiến độ học tập
CREATE TABLE progress (
    progress_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    video_position INT DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completion_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (enrollment_id, lesson_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id)
);

-- Bảng đánh giá khóa học
CREATE TABLE review (
                        review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        course_id BIGINT NOT NULL,
                        student_id BIGINT NOT NULL,
                        rating INT NOT NULL COMMENT 'Đánh giá từ 1-5 sao',
                        comment TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        UNIQUE KEY (course_id, student_id),
                        FOREIGN KEY (course_id) REFERENCES course(course_id),
                        FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Bảng bài kiểm tra/quiz
CREATE TABLE quiz (
                      quiz_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      lesson_id BIGINT NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      time_limit INT COMMENT 'Thời gian làm bài tính bằng phút',
                      passing_score INT COMMENT 'Điểm đạt yêu cầu (tính theo %)',
                      attempts_allowed INT DEFAULT 1 COMMENT 'Số lần được phép làm bài',
                      is_active BOOLEAN DEFAULT TRUE,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- Bảng câu hỏi quiz
CREATE TABLE quiz_question (
                               question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               quiz_id BIGINT NOT NULL,
                               question_text TEXT NOT NULL,
                               question_type VARCHAR(50) COMMENT 'multiple_choice, true_false, essay',
                               points INT DEFAULT 1,
                               position INT COMMENT 'Thứ tự câu hỏi trong bài quiz',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE
);

-- Bảng đáp án cho câu hỏi
CREATE TABLE quiz_answer (
                             answer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             question_id BIGINT NOT NULL,
                             answer_text TEXT NOT NULL,
                             is_correct BOOLEAN DEFAULT FALSE,
                             position INT COMMENT 'Thứ tự đáp án trong câu hỏi',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (question_id) REFERENCES quiz_question(question_id) ON DELETE CASCADE
);

-- Bảng lưu các lần làm quiz của học viên
CREATE TABLE quiz_attempt (
                              attempt_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              quiz_id BIGINT NOT NULL,
                              student_id BIGINT NOT NULL,
                              score TINYINT COMMENT 'Điểm số đạt được',
                              start_time DATETIME NOT NULL,
                              submit_time DATETIME,
                              time_spent INT COMMENT 'Thời gian làm bài tính bằng giây',
                              status VARCHAR(50) DEFAULT 'in_progress' COMMENT 'in_progress, completed, passed, failed',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id),
                              FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Bảng lưu câu trả lời của học viên
CREATE TABLE student_answer (
                                student_answer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                attempt_id BIGINT NOT NULL,
                                question_id BIGINT NOT NULL,
                                answer_id BIGINT COMMENT 'ID đáp án đã chọn (đối với câu hỏi trắc nghiệm)',
                                text_answer TEXT COMMENT 'Câu trả lời dạng văn bản (đối với câu hỏi tự luận)',
                                is_correct BOOLEAN DEFAULT FALSE,
                                points_earned INT DEFAULT 0,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (attempt_id) REFERENCES quiz_attempt(attempt_id) ON DELETE CASCADE,
                                FOREIGN KEY (question_id) REFERENCES quiz_question(question_id),
                                FOREIGN KEY (answer_id) REFERENCES quiz_answer(answer_id)
);

-- Bảng chứng chỉ
CREATE TABLE certificate (
                             certificate_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             enrollment_id BIGINT NOT NULL,
                             certificate_code VARCHAR(100) UNIQUE NOT NULL,
                             issue_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                             certificate_path VARCHAR(255) COMMENT 'Đường dẫn đến file chứng chỉ',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY (enrollment_id),
                             FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id)
);

-- Bảng thông báo
CREATE TABLE notification (
                              notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              account_id BIGINT NOT NULL,
                              title VARCHAR(255) NOT NULL,
                              content TEXT,
                              is_read BOOLEAN DEFAULT FALSE,
                              notification_type VARCHAR(50) COMMENT 'course_update, quiz_reminder, certificate_issued, ...',
                              reference_id INT COMMENT 'ID tham chiếu đến đối tượng liên quan (course_id, quiz_id, ...)',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (account_id) REFERENCES account(account_id)
); SHOW CREATE TABLE account;

-- Bảng lưu trữ khóa học mà học viên đã mua
CREATE TABLE student_course (
                                enrollment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                student_id BIGINT NOT NULL,
                                course_id BIGINT NOT NULL,

    -- Thông tin giao dịch
                                purchase_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                amount_paid DECIMAL(10, 2) NOT NULL,
                                payment_method VARCHAR(50),
                                transaction_id VARCHAR(100),
                                payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',

    -- Thông tin học tập
                                progress_percentage TINYINT DEFAULT 0,
                                last_access_date DATETIME,
                                completion_date DATETIME,
                                expiration_date DATETIME,
                                is_active BOOLEAN DEFAULT TRUE,

    -- Thông tin thời gian
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Ràng buộc
                                FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                                FOREIGN KEY (course_id) REFERENCES course(course_id),
                                UNIQUE KEY unique_student_course (student_id, course_id)
);
-- Bảng giỏ hàng
CREATE TABLE cart_item (
                           cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           student_id BIGINT NOT NULL,
                           course_id BIGINT NOT NULL,
                           quantity INT DEFAULT 1,
                           price DECIMAL(10, 2),
                           discount_price DECIMAL(10, 2),
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           UNIQUE KEY (student_id, course_id),
                           FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                           FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

ALTER TABLE student
    CHANGE COLUMN class class_name VARCHAR(255);



-- INSERT
-- =============================================
-- File: insert_all_data.sql
-- Mục tiêu: Thêm dữ liệu mẫu đầy đủ cho hệ thống quản lý học viên
-- Đảm bảo thứ tự Insert đúng chuẩn, không lỗi FOREIGN KEY
-- =============================================

-- 1. Insert roles
INSERT INTO roles (role_name, description) VALUES
('admin', 'System administrator with full access'),
('teacher', 'Teacher role with academic privileges'),
('student', 'Student role with learning access');

-- 2. Insert account (5 học viên mẫu)
INSERT INTO account (username, email, password, full_name, role_id)
VALUES
('student20', 'student20@example.com', '123456', 'Student 20', 3),
('student17', 'student17@example.com', '123456', 'Student 17', 3),
('student18', 'student18@example.com', '123456', 'Student 18', 3),
('student19', 'student19@example.com', '123456', 'Student 19', 3),
('student21', 'student21@example.com', '123456', 'Student 21', 3);

-- 3. Insert student
INSERT INTO student (student_id, class_name)
VALUES
(1, 'A1'),
(2, 'B2'),
(3, 'C3'),
(4, 'D4'),
(5, 'E5');

-- 4. Insert course_category
INSERT INTO course_category (category_name, description, icon_path)
VALUES
('Lập trình', 'Các khóa học về lập trình, phát triển phần mềm', '/icons/programming.png'),
('Thiết kế', 'Các khóa học về thiết kế giao diện, đồ họa', '/icons/design.png'),
('Marketing', 'Các khóa học về digital marketing, SEO', '/icons/marketing.png'),
('Ngoại ngữ', 'Các khóa học về ngôn ngữ', '/icons/language.png'),
('Kỹ năng mềm', 'Các khóa học về kỹ năng giao tiếp, lãnh đạo', '/icons/soft-skills.png');

-- 5. Insert course (dùng ID category đúng)
INSERT INTO course (title, description, category_id, thumbnail_path, price, discount_price, duration, level, is_published, published_at, is_featured, rating, total_students, total_reviews)
VALUES
('Nhập môn lập trình với Python', 'Học Python cơ bản', 1, '/img/courses/python-intro.jpg', 0, NULL, 240, 'beginner', 1, NOW(), 1, 4.7, 15280, 1245),
('HTML & CSS cơ bản', 'Cơ bản HTML & CSS', 1, '/img/courses/html-css-basics.jpg', 0, NULL, 180, 'beginner', 1, NOW(), 0, 4.5, 12450, 980),
('JavaScript cho người mới bắt đầu', 'JavaScript cơ bản', 1, '/img/courses/js-beginners.jpg', 0, NULL, 210, 'beginner', 1, NOW(), 1, 4.6, 10320, 876);

-- 6. Insert module
INSERT INTO module (course_id, title, description, position, is_free)
VALUES
(1, 'Giới thiệu về Python', 'Python cho người mới', 1, TRUE),
(2, 'Giới thiệu HTML', 'HTML cơ bản', 1, TRUE),
(3, 'Giới thiệu JavaScript', 'JavaScript cơ bản', 1, TRUE);

-- 7. Insert lesson
INSERT INTO lesson (module_id, title, description, duration, position, is_free)
VALUES
(1, 'Hello World với Python', 'In ra màn hình Hello World', 15, 1, TRUE),
(2, 'Cấu trúc HTML cơ bản', 'HTML structure', 20, 1, TRUE),
(3, 'Biến trong JavaScript', 'Cách khai báo biến', 20, 1, TRUE);

-- 8. Insert video
INSERT INTO video (lesson_id, title, video_url, thumbnail_path, duration, is_downloadable)
VALUES
(1, 'Python Hello World', 'https://videos.example.com/python-hello.mp4', '/thumbnails/python-hello.jpg', 900, FALSE),
(2, 'HTML Basics', 'https://videos.example.com/html-basics.mp4', '/thumbnails/html-basics.jpg', 800, FALSE),
(3, 'JavaScript Variables', 'https://videos.example.com/js-variables.mp4', '/thumbnails/js-variables.jpg', 1000, FALSE);

-- 9. Insert enrollment (gán đúng student_id)
INSERT INTO enrollment (course_id, student_id, enrollment_date, payment_status, payment_amount, payment_method, transaction_id, payment_date)
VALUES
(1, 1, NOW(), 'completed', 0, NULL, NULL, NOW()),
(2, 2, NOW(), 'completed', 0, NULL, NULL, NOW()),
(3, 3, NOW(), 'completed', 0, NULL, NULL, NOW());

-- 10. Insert progress
INSERT INTO progress (enrollment_id, lesson_id, video_position, is_completed, last_accessed_at, completion_date)
VALUES
(1, 1, 900, TRUE, NOW(), NOW()),
(2, 2, 800, TRUE, NOW(), NOW()),
(3, 3, 1000, FALSE, NOW(), NULL);

-- 11. Insert quiz, quiz_question, quiz_answer, quiz_attempt, student_answer (có thể thêm sau nếu cần)

-- 12. Insert notification (ví dụ nhẹ)
INSERT INTO notification (account_id, title, content, is_read, notification_type)
VALUES
(1, 'Chào mừng bạn', 'Chào mừng đến hệ thống học viên', FALSE, 'system_welcome');

-- =============================================
-- 📌 Đảm bảo rằng bạn kiểm tra lại ID khi import thực tế để tránh conflict tự tăng
-- =============================================
