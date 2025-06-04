use quanlyhocvien
select * from student
-- Bảng roles
CREATE TABLE roles (
                       role_id INT PRIMARY KEY AUTO_INCREMENT,
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
                         account_id INT PRIMARY KEY AUTO_INCREMENT,
                         username VARCHAR(50) UNIQUE NOT NULL,
                         email VARCHAR(100) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         full_name VARCHAR(100) NOT NULL,
                         date_of_birth DATE,
                         phone_number VARCHAR(15),
                         address VARCHAR(255),
                         gender VARCHAR(10),
                         avatar_path varchar(100),
                         role_id INT NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
-- Bảng admin (bảng con - chuyên biệt hóa)
CREATE TABLE admin (
                       admin_id INT PRIMARY KEY,
    --   department VARCHAR(50),
                       FOREIGN KEY (admin_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Bảng teacher (bảng con - chuyên biệt hóa)
CREATE TABLE teacher (
                         teacher_id INT PRIMARY KEY,
                         subject_specialization VARCHAR(100),
                         qualification VARCHAR(100),
                         hire_date DATE,
                         FOREIGN KEY (teacher_id) REFERENCES account(account_id) ON DELETE CASCADE
);

-- Bảng student (bảng con - chuyên biệt hóa)
CREATE TABLE student (
                         student_id INT PRIMARY KEY,
    --   admission_number VARCHAR(20) UNIQUE,
                         class VARCHAR(20),
--     enrollment_date DATE,
--     graduation_year INT,
                         FOREIGN KEY (student_id) REFERENCES account(account_id) ON DELETE CASCADE
);
CREATE TABLE verification_token (
                                    token_id INT PRIMARY KEY AUTO_INCREMENT,
                                    account_id INT NOT NULL,
                                    token VARCHAR(255) NOT NULL,
                                    expiry_date DATETIME NOT NULL,
                                    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
);
ALTER TABLE account
    ADD COLUMN is_email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE account MODIFY COLUMN full_name VARCHAR(100) NULL;
INSERT INTO roles (role_name)
VALUES
    ('STUDENT'),
    ('ADMIN'),
    ('TEACHER');
select * from student
select * from account
select * from roles
select * from verification_token
select * from admin
select * from teacher
-- drop table student
-- drop table accounts
-- drop table roles
delete from verification_token where account_id = 11
delete from account where account_id = 14
ALTER TABLE account
    add column  avatar_path varchar(100)

-- Bảng danh mục khóa học
CREATE TABLE course_category (
                                 category_id INT PRIMARY KEY AUTO_INCREMENT,
                                 category_name VARCHAR(100) NOT NULL,
                                 description VARCHAR(255),
                                 icon_path VARCHAR(100),
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bảng khóa học
CREATE TABLE course (
                        course_id INT PRIMARY KEY AUTO_INCREMENT,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        category_id INT NOT NULL,
                        thumbnail_path VARCHAR(255),
                        price DECIMAL(10, 2) DEFAULT 0,
                        discount_price DECIMAL(10, 2),
                        duration INT COMMENT 'Tổng thời gian khóa học tính theo phút',
                        level VARCHAR(50) COMMENT 'Cơ bản, Trung cấp, Nâng cao',
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

-- Bảng chương/module của khóa học
CREATE TABLE module (
                        module_id INT PRIMARY KEY AUTO_INCREMENT,
                        course_id INT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        position INT COMMENT 'Thứ tự của module trong khóa học',
                        is_free BOOLEAN DEFAULT FALSE COMMENT 'Module miễn phí để xem thử',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

-- Bảng bài học
CREATE TABLE lesson (
                        lesson_id INT PRIMARY KEY AUTO_INCREMENT,
                        module_id INT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        duration INT COMMENT 'Thời lượng bài học tính bằng phút',
                        position INT COMMENT 'Thứ tự của bài học trong module',
                        is_free BOOLEAN DEFAULT FALSE COMMENT 'Bài học miễn phí để xem thử',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (module_id) REFERENCES module(module_id) ON DELETE CASCADE
);

-- Bảng video bài học
CREATE TABLE video (
                       video_id INT PRIMARY KEY AUTO_INCREMENT,
                       lesson_id INT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       video_url VARCHAR(255) NOT NULL,
                       thumbnail_path VARCHAR(255),
                       duration INT COMMENT 'Thời lượng video tính bằng giây',
                       is_downloadable BOOLEAN DEFAULT FALSE,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- Bảng tài liệu đính kèm bài học
CREATE TABLE lesson_attachment (
                                   attachment_id INT PRIMARY KEY AUTO_INCREMENT,
                                   lesson_id INT NOT NULL,
                                   title VARCHAR(255) NOT NULL,
                                   file_path VARCHAR(255) NOT NULL,
                                   file_type VARCHAR(50) COMMENT 'Loại tài liệu (PDF, DOC, ...)',
                                   file_size INT COMMENT 'Kích thước file tính bằng KB',
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
);

-- Bảng đăng ký khóa học
CREATE TABLE enrollment (
                            enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
                            course_id INT NOT NULL,
                            student_id INT NOT NULL,
                            enrollment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                            expiry_date DATETIME COMMENT 'Ngày hết hạn truy cập khóa học (nếu có)',
                            payment_status VARCHAR(50) DEFAULT 'pending' COMMENT 'pending, completed, refunded, ...',
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

-- Bảng tiến độ học tập
CREATE TABLE progress (
                          progress_id INT PRIMARY KEY AUTO_INCREMENT,
                          enrollment_id INT NOT NULL,
                          lesson_id INT NOT NULL,
                          video_position INT DEFAULT 0 COMMENT 'Vị trí đã xem đến trong video (tính bằng giây)',
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
                        review_id INT PRIMARY KEY AUTO_INCREMENT,
                        course_id INT NOT NULL,
                        student_id INT NOT NULL,
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
                      quiz_id INT PRIMARY KEY AUTO_INCREMENT,
                      lesson_id INT NOT NULL,
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
                               question_id INT PRIMARY KEY AUTO_INCREMENT,
                               quiz_id INT NOT NULL,
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
                             answer_id INT PRIMARY KEY AUTO_INCREMENT,
                             question_id INT NOT NULL,
                             answer_text TEXT NOT NULL,
                             is_correct BOOLEAN DEFAULT FALSE,
                             position INT COMMENT 'Thứ tự đáp án trong câu hỏi',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (question_id) REFERENCES quiz_question(question_id) ON DELETE CASCADE
);

-- Bảng lưu các lần làm quiz của học viên
CREATE TABLE quiz_attempt (
                              attempt_id INT PRIMARY KEY AUTO_INCREMENT,
                              quiz_id INT NOT NULL,
                              student_id INT NOT NULL,
                              score INT COMMENT 'Điểm số đạt được',
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
                                student_answer_id INT PRIMARY KEY AUTO_INCREMENT,
                                attempt_id INT NOT NULL,
                                question_id INT NOT NULL,
                                answer_id INT COMMENT 'ID đáp án đã chọn (đối với câu hỏi trắc nghiệm)',
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
                             certificate_id INT PRIMARY KEY AUTO_INCREMENT,
                             enrollment_id INT NOT NULL,
                             certificate_code VARCHAR(100) UNIQUE NOT NULL,
                             issue_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                             certificate_path VARCHAR(255) COMMENT 'Đường dẫn đến file chứng chỉ',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY (enrollment_id),
                             FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id)
);

-- Bảng thông báo
CREATE TABLE notification (
                              notification_id INT PRIMARY KEY AUTO_INCREMENT,
                              account_id INT NOT NULL,
                              title VARCHAR(255) NOT NULL,
                              content TEXT,
                              is_read BOOLEAN DEFAULT FALSE,
                              notification_type VARCHAR(50) COMMENT 'course_update, quiz_reminder, certificate_issued, ...',
                              reference_id INT COMMENT 'ID tham chiếu đến đối tượng liên quan (course_id, quiz_id, ...)',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (account_id) REFERENCES account(account_id)
);
-- Bảng lưu trữ khóa học mà học viên đã mua
CREATE TABLE student_course (
                                enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
                                student_id INT NOT NULL,
                                course_id INT NOT NULL,

    -- Thông tin giao dịch
                                purchase_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                amount_paid DECIMAL(10, 2) NOT NULL,
                                payment_method VARCHAR(50),
                                transaction_id VARCHAR(100),
                                payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',

    -- Thông tin học tập
                                progress_percentage INT DEFAULT 0,
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
                           cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
                           student_id INT NOT NULL,
                           course_id INT NOT NULL,
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

drop table course_categories
