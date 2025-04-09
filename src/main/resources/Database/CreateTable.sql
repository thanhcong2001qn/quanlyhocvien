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
    is_email_verified BOOLEAN DEFAULT FALSE,
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

-- Bảng danh mục khóa học (categories)
CREATE TABLE course_categories
(
    category_id   INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Bảng khóa học (courses)
CREATE TABLE courses
(
    course_id    INT PRIMARY KEY AUTO_INCREMENT,
    course_code  VARCHAR(20) UNIQUE NOT NULL,
    course_name  VARCHAR(255)       NOT NULL,
    description  TEXT,
    category_id  INT,
    teacher_id   INT                NOT NULL,
    max_students INT,
    price        DECIMAL(10, 2) DEFAULT 0,
    start_date   DATE,
    end_date     DATE,
    image_path   VARCHAR(255),
    status       ENUM('draft', 'published', 'archived') DEFAULT 'draft',
    created_at   DATETIME       DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES course_categories (category_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher (teacher_id)
);

-- Bảng đăng ký khóa học (enrollments)
CREATE TABLE course_enrollments
(
    enrollment_id      INT PRIMARY KEY AUTO_INCREMENT,
    course_id          INT NOT NULL,
    student_id         INT NOT NULL,
    enrollment_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    status             ENUM('pending', 'active', 'completed', 'dropped') DEFAULT 'pending',
    completion_date    DATETIME,
    grade              DECIMAL(5, 2),
    certificate_issued BOOLEAN  DEFAULT FALSE,
    FOREIGN KEY (course_id) REFERENCES courses (course_id),
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    UNIQUE (course_id, student_id)
);

-- Bảng nội dung khóa học (modules)
CREATE TABLE course_modules
(
    module_id   INT PRIMARY KEY AUTO_INCREMENT,
    course_id   INT          NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT          NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses (course_id)
);

-- Bảng bài học (lessons)
CREATE TABLE lessons
(
    lesson_id        INT PRIMARY KEY AUTO_INCREMENT,
    module_id        INT          NOT NULL,
    lesson_name      VARCHAR(255) NOT NULL,
    content          TEXT,
    video_path       VARCHAR(255),
    duration_minutes INT,
    order_index      INT          NOT NULL,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (module_id) REFERENCES course_modules (module_id)
);

-- Bảng đánh giá khóa học (reviews)
CREATE TABLE course_reviews
(
    review_id  INT PRIMARY KEY AUTO_INCREMENT,
    course_id  INT NOT NULL,
    student_id INT NOT NULL,
    rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses (course_id),
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    UNIQUE (course_id, student_id)
);

-- Bảng bài tập (assignments)
CREATE TABLE assignments
(
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id     INT          NOT NULL,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    due_date      DATETIME,
    max_score     DECIMAL(5, 2),
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses (course_id)
);

-- Bảng nộp bài tập (submissions)
CREATE TABLE assignment_submissions
(
    submission_id   INT PRIMARY KEY AUTO_INCREMENT,
    assignment_id   INT NOT NULL,
    student_id      INT NOT NULL,
    submission_text TEXT,
    file_path       VARCHAR(255),
    submitted_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    score           DECIMAL(5, 2),
    feedback        TEXT,
    graded_by       INT,
    graded_at       DATETIME,
    FOREIGN KEY (assignment_id) REFERENCES assignments (assignment_id),
    FOREIGN KEY (student_id) REFERENCES student (student_id),
    FOREIGN KEY (graded_by) REFERENCES teacher (teacher_id)
);