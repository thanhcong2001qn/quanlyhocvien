package com.dacs.quanlyhocvien.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DatabaseSchemaExtractor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DatabaseSchemaExtractor() {
    }

    public String extractJsonSchema() {
        try {
            Map<String, List<String>> schema = new LinkedHashMap<>();

            // Lấy danh sách bảng
            List<String> tables = jdbcTemplate.queryForList(
                    "SHOW TABLES",
                    String.class
            );

            // Lặp qua từng bảng để lấy danh sách cột
            for (String table : tables) {
                List<String> columns = jdbcTemplate.queryForList(
                        "SHOW COLUMNS FROM " + table,
                        String.class
                );
                schema.put(table, columns);
            }

            // Chuyển sang JSON
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        } catch (Exception e) {
            return "{ \"error\": \"Lỗi khi trích xuất schema: " + e.getMessage() + "\" }";
        }
    }

    public String getCompleteSchema() {
        return """
                -- Table: roles
                CREATE TABLE roles (
                    role_id INT PRIMARY KEY, -- ID vai trò
                    role_name VARCHAR(50) UNIQUE NOT NULL, -- Tên vai trò
                    description VARCHAR(255), -- Mô tả vai trò
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP -- Thời gian tạo
                );
                
                -- Table: account
                CREATE TABLE account (
                    account_id INT PRIMARY KEY AUTO_INCREMENT, -- ID tài khoản
                    username VARCHAR(50) UNIQUE NOT NULL, -- Tên đăng nhập
                    email VARCHAR(100) UNIQUE NOT NULL, -- Email
                    password VARCHAR(255) NOT NULL, -- Mật khẩu
                    full_name VARCHAR(100), -- Họ và tên
                    date_of_birth DATE, -- Ngày sinh
                    phone_number VARCHAR(15), -- Số điện thoại
                    address VARCHAR(255), -- Địa chỉ
                    gender VARCHAR(10), -- Giới tính
                    avatar_path VARCHAR(100), -- Đường dẫn ảnh đại diện
                    role_id INT NOT NULL, -- ID vai trò
                    is_active BOOLEAN DEFAULT TRUE, -- Trạng thái hoạt động
                    is_email_verified BOOLEAN DEFAULT FALSE, -- Trạng thái xác thực email
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- Thời gian cập nhật cuối cùng
                );
                
                -- Table: admin
                CREATE TABLE admin (
                    admin_id INT PRIMARY KEY, -- ID quản trị viên
                    FOREIGN KEY (admin_id) REFERENCES account(account_id) ON DELETE CASCADE
                );
                
                -- Table: teacher
                CREATE TABLE teacher (
                    teacher_id INT PRIMARY KEY, -- ID giáo viên
                    subject_specialization VARCHAR(100), -- Chuyên môn môn học
                    qualification VARCHAR(100), -- Bằng cấp
                    hire_date DATE, -- Ngày thuê
                    FOREIGN KEY (teacher_id) REFERENCES account(account_id) ON DELETE CASCADE
                );
                
                -- Table: student
                CREATE TABLE student (
                    student_id INT PRIMARY KEY, -- ID học sinh/sinh viên
                    class_name VARCHAR(255), -- Tên lớp học (nếu có)
                    FOREIGN KEY (student_id) REFERENCES account(account_id) ON DELETE CASCADE
                );
                
                -- Table: verification_token
                CREATE TABLE verification_token (
                    token_id INT PRIMARY KEY AUTO_INCREMENT, -- ID mã xác minh
                    account_id INT NOT NULL, -- ID tài khoản
                    token VARCHAR(255) NOT NULL, -- Mã xác minh
                    expiry_date DATETIME NOT NULL, -- Ngày hết hạn của mã
                    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE
                );
                
                -- Table: course_category
                CREATE TABLE course_category (
                    category_id INT PRIMARY KEY AUTO_INCREMENT, -- ID danh mục khóa học
                    category_name VARCHAR(100) NOT NULL, -- Tên danh mục
                    description VARCHAR(255), -- Mô tả danh mục
                    icon_path VARCHAR(100), -- Đường dẫn icon
                    is_active BOOLEAN DEFAULT TRUE, -- Trạng thái hoạt động
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- Thời gian cập nhật cuối cùng
                );
                
                -- Table: course
                CREATE TABLE course (
                    course_id INT PRIMARY KEY AUTO_INCREMENT, -- ID khóa học
                    title VARCHAR(255) NOT NULL, -- Tiêu đề khóa học
                    description TEXT, -- Mô tả khóa học
                    category_id INT NOT NULL, -- ID danh mục khóa học
                    thumbnail_path VARCHAR(255), -- Đường dẫn ảnh thu nhỏ
                    price DECIMAL(10,2) DEFAULT 0, -- Giá khóa học
                    discount_price DECIMAL(10,2), -- Giá sau khi giảm giá
                    duration INT, -- Thời lượng khóa học (tính bằng phút hoặc giờ)
                    level VARCHAR(50), -- Cấp độ khóa học (ví dụ: cơ bản, trung cấp, nâng cao)
                    is_published BOOLEAN DEFAULT FALSE, -- Trạng thái công khai khóa học
                    published_at DATETIME, -- Ngày/thời gian khóa học ĐƯỢC CÔNG KHAI cho người dùng
                    is_featured BOOLEAN DEFAULT FALSE, -- Khóa học nổi bật
                    rating FLOAT DEFAULT 0, -- Đánh giá trung bình
                    total_students INT DEFAULT 0, -- Tổng số học viên
                    total_reviews INT DEFAULT 0, -- Tổng số đánh giá
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian KHÓA HỌC ĐƯỢC TẠO trong hệ thống (nội bộ)
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (category_id) REFERENCES course_category(category_id)
                );
                
                -- Table: module
                CREATE TABLE module (
                    module_id INT PRIMARY KEY AUTO_INCREMENT, -- ID module
                    course_id INT NOT NULL, -- ID khóa học
                    title VARCHAR(255) NOT NULL, -- Tiêu đề module
                    description TEXT, -- Mô tả module
                    position INT, -- Vị trí của module trong khóa học
                    is_free BOOLEAN DEFAULT FALSE, -- Module có miễn phí không
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
                );
                
                -- Table: lesson
                CREATE TABLE lesson (
                    lesson_id INT PRIMARY KEY AUTO_INCREMENT, -- ID bài học
                    module_id INT NOT NULL, -- ID module
                    title VARCHAR(255) NOT NULL, -- Tiêu đề bài học
                    description TEXT, -- Mô tả bài học
                    duration INT, -- Thời lượng bài học (tính bằng phút)
                    position INT, -- Vị trí của bài học trong module
                    is_free BOOLEAN DEFAULT FALSE, -- Bài học có miễn phí không
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (module_id) REFERENCES module(module_id) ON DELETE CASCADE
                );
                
                -- Table: video
                CREATE TABLE video (
                    video_id INT PRIMARY KEY AUTO_INCREMENT, -- ID video
                    lesson_id INT NOT NULL, -- ID bài học
                    title VARCHAR(255) NOT NULL, -- Tiêu đề video
                    video_url VARCHAR(255) NOT NULL, -- URL video
                    thumbnail_path VARCHAR(255), -- Đường dẫn ảnh thu nhỏ video
                    duration INT, -- Thời lượng video (tính bằng giây)
                    is_downloadable BOOLEAN DEFAULT FALSE, -- Có cho phép tải xuống không
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
                );
                
                -- Table: lesson_attachment
                CREATE TABLE lesson_attachment (
                    attachment_id INT PRIMARY KEY AUTO_INCREMENT, -- ID tệp đính kèm
                    lesson_id INT NOT NULL, -- ID bài học
                    title VARCHAR(255) NOT NULL, -- Tiêu đề tệp đính kèm
                    file_path VARCHAR(255) NOT NULL, -- Đường dẫn tệp
                    file_type VARCHAR(50), -- Loại tệp
                    file_size INT, -- Kích thước tệp (bytes)
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
                );
                
                -- Table: enrollment
                CREATE TABLE enrollment (
                    enrollment_id INT PRIMARY KEY AUTO_INCREMENT, -- ID đăng ký học
                    course_id INT NOT NULL, -- ID khóa học
                    student_id INT NOT NULL, -- ID học sinh/sinh viên
                    enrollment_date DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày đăng ký
                    expiry_date DATETIME, -- Ngày hết hạn khóa học
                    payment_status VARCHAR(50) DEFAULT 'pending', -- Trạng thái thanh toán
                    payment_amount DECIMAL(10,2), -- Số tiền thanh toán
                    payment_method VARCHAR(50), -- Phương thức thanh toán
                    transaction_id VARCHAR(100), -- ID giao dịch
                    payment_date DATETIME, -- Ngày thanh toán
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    UNIQUE KEY (course_id, student_id),
                    FOREIGN KEY (course_id) REFERENCES course(course_id),
                    FOREIGN KEY (student_id) REFERENCES student(student_id)
                );
                
                -- Table: progress
                CREATE TABLE progress (
                    progress_id INT PRIMARY KEY AUTO_INCREMENT, -- ID tiến độ
                    enrollment_id INT NOT NULL, -- ID đăng ký học
                    lesson_id INT NOT NULL, -- ID bài học
                    video_position INT DEFAULT 0, -- Vị trí video hiện tại (giây)
                    is_completed BOOLEAN DEFAULT FALSE, -- Trạng thái hoàn thành bài học
                    last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian truy cập cuối cùng
                    completion_date DATETIME, -- Ngày hoàn thành bài học
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    UNIQUE KEY (enrollment_id, lesson_id),
                    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id) ON DELETE CASCADE,
                    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id)
                );
                
                -- Table: review
                CREATE TABLE review (
                    review_id INT PRIMARY KEY AUTO_INCREMENT, -- ID đánh giá
                    course_id INT NOT NULL, -- ID khóa học
                    student_id INT NOT NULL, -- ID học sinh/sinh viên
                    rating INT NOT NULL, -- Điểm đánh giá (1-5 sao)
                    comment TEXT, -- Bình luận
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo đánh giá
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    UNIQUE KEY (course_id, student_id),
                    FOREIGN KEY (course_id) REFERENCES course(course_id),
                    FOREIGN KEY (student_id) REFERENCES student(student_id)
                );
                
                -- Table: quiz
                CREATE TABLE quiz (
                    quiz_id INT PRIMARY KEY AUTO_INCREMENT, -- ID bài kiểm tra
                    lesson_id INT NOT NULL, -- ID bài học
                    title VARCHAR(255) NOT NULL, -- Tiêu đề bài kiểm tra
                    description TEXT, -- Mô tả bài kiểm tra
                    time_limit INT, -- Thời gian giới hạn làm bài (phút)
                    passing_score INT, -- Điểm đạt
                    attempts_allowed INT DEFAULT 1, -- Số lần làm bài cho phép
                    is_active BOOLEAN DEFAULT TRUE, -- Trạng thái hoạt động
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (lesson_id) REFERENCES lesson(lesson_id) ON DELETE CASCADE
                );
                
                -- Table: quiz_question
                CREATE TABLE quiz_question (
                    question_id INT PRIMARY KEY AUTO_INCREMENT, -- ID câu hỏi
                    quiz_id INT NOT NULL, -- ID bài kiểm tra
                    question_text TEXT NOT NULL, -- Nội dung câu hỏi
                    question_type VARCHAR(50), -- Loại câu hỏi (ví dụ: trắc nghiệm, tự luận)
                    points INT DEFAULT 1, -- Điểm cho câu hỏi
                    position INT, -- Vị trí câu hỏi trong bài kiểm tra
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id) ON DELETE CASCADE
                );
                
                -- Table: quiz_answer
                CREATE TABLE quiz_answer (
                    answer_id INT PRIMARY KEY AUTO_INCREMENT, -- ID câu trả lời
                    question_id INT NOT NULL, -- ID câu hỏi
                    answer_text TEXT NOT NULL, -- Nội dung câu trả lời
                    is_correct BOOLEAN DEFAULT FALSE, -- Là đáp án đúng
                    position INT, -- Vị trí câu trả lời
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    FOREIGN KEY (question_id) REFERENCES quiz_question(question_id) ON DELETE CASCADE
                );
                
                -- Table: quiz_attempt
                CREATE TABLE quiz_attempt (
                    attempt_id INT PRIMARY KEY AUTO_INCREMENT, -- ID lần làm bài kiểm tra
                    quiz_id INT NOT NULL, -- ID bài kiểm tra
                    student_id INT NOT NULL, -- ID học sinh/sinh viên
                    score INT, -- Điểm số đạt được
                    start_time DATETIME NOT NULL, -- Thời gian bắt đầu làm bài
                    submit_time DATETIME, -- Thời gian nộp bài
                    time_spent INT, -- Thời gian làm bài (giây)
                    status VARCHAR(50) DEFAULT 'in_progress', -- Trạng thái làm bài (đang làm, đã hoàn thành)
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id),
                    FOREIGN KEY (student_id) REFERENCES student(student_id)
                );
                
                -- Table: student_answer
                CREATE TABLE student_answer (
                    student_answer_id INT PRIMARY KEY AUTO_INCREMENT, -- ID câu trả lời của học sinh
                    attempt_id INT NOT NULL, -- ID lần làm bài kiểm tra
                    question_id INT NOT NULL, -- ID câu hỏi
                    answer_id INT, -- ID câu trả lời (nếu là trắc nghiệm)
                    text_answer TEXT, -- Câu trả lời dạng văn bản (nếu là tự luận)
                    is_correct BOOLEAN DEFAULT FALSE, -- Câu trả lời có đúng không
                    points_earned INT DEFAULT 0, -- Điểm kiếm được từ câu hỏi
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    FOREIGN KEY (attempt_id) REFERENCES quiz_attempt(attempt_id) ON DELETE CASCADE,
                    FOREIGN KEY (question_id) REFERENCES quiz_question(question_id),
                    FOREIGN KEY (answer_id) REFERENCES quiz_answer(answer_id)
                );
                
                -- Table: certificate
                CREATE TABLE certificate (
                    certificate_id INT PRIMARY KEY AUTO_INCREMENT, -- ID chứng chỉ
                    enrollment_id INT NOT NULL, -- ID đăng ký học
                    certificate_code VARCHAR(100) UNIQUE NOT NULL, -- Mã chứng chỉ
                    issue_date DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày cấp chứng chỉ
                    certificate_path VARCHAR(255), -- Đường dẫn tệp chứng chỉ
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    UNIQUE KEY (enrollment_id),
                    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id)
                );
                
                -- Table: notification
                CREATE TABLE notification (
                    notification_id INT PRIMARY KEY AUTO_INCREMENT, -- ID thông báo
                    account_id INT NOT NULL, -- ID tài khoản nhận thông báo
                    title VARCHAR(255) NOT NULL, -- Tiêu đề thông báo
                    content TEXT, -- Nội dung thông báo
                    is_read BOOLEAN DEFAULT FALSE, -- Trạng thái đã đọc
                    notification_type VARCHAR(50), -- Loại thông báo (ví dụ: hệ thống, khóa học mới)
                    reference_id INT, -- ID tham chiếu đến đối tượng liên quan (ví dụ: course_id)
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    FOREIGN KEY (account_id) REFERENCES account(account_id)
                );
                
                -- Table: student_course
                CREATE TABLE student_course (
                    enrollment_id INT PRIMARY KEY AUTO_INCREMENT, -- ID đăng ký khóa học của học sinh
                    student_id INT NOT NULL, -- ID học sinh/sinh viên
                    course_id INT NOT NULL, -- ID khóa học
                    purchase_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Ngày mua khóa học
                    amount_paid DECIMAL(10,2) NOT NULL, -- Số tiền đã thanh toán
                    payment_method VARCHAR(50), -- Phương thức thanh toán
                    transaction_id VARCHAR(100), -- ID giao dịch
                    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING', -- Trạng thái thanh toán
                    progress_percentage INT DEFAULT 0, -- Phần trăm tiến độ khóa học
                    last_access_date DATETIME, -- Ngày truy cập khóa học cuối cùng
                    completion_date DATETIME, -- Ngày hoàn thành khóa học
                    expiration_date DATETIME, -- Ngày hết hạn khóa học
                    is_active BOOLEAN DEFAULT TRUE, -- Khóa học có hoạt động không
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                    FOREIGN KEY (course_id) REFERENCES course(course_id),
                    UNIQUE KEY unique_student_course (student_id, course_id)
                );
                
                -- Table: cart_item
                CREATE TABLE cart_item (
                    cart_item_id INT PRIMARY KEY AUTO_INCREMENT, -- ID mục giỏ hàng
                    student_id INT NOT NULL, -- ID học sinh/sinh viên
                    course_id INT NOT NULL, -- ID khóa học
                    quantity INT DEFAULT 1, -- Số lượng khóa học (thường là 1)
                    price DECIMAL(10,2), -- Giá của khóa học tại thời điểm thêm vào giỏ
                    discount_price DECIMAL(10,2), -- Giá giảm của khóa học tại thời điểm thêm vào giỏ
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- Thời gian tạo
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Thời gian cập nhật cuối cùng
                    UNIQUE KEY (student_id, course_id),
                    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
                    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
                );
        """;
    }
}
