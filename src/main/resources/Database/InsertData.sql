INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'System administrator'),
('TEACHER', 'Teacher role'),
('STUDENT', 'Student role');

INSERT INTO account
(username, email, password, full_name, role_id, is_active, is_email_verified)
VALUES
('admin01', 'admin@gmail.com', '123456', 'Admin System', 1, TRUE, TRUE),
('teacher01', 'teacher@gmail.com', '123456', 'Nguyen Van Teacher', 2, TRUE, TRUE),
('student01', 'student1@gmail.com', '123456', 'Nguyen Van A', 3, TRUE, TRUE),
('student02', 'student2@gmail.com', '123456', 'Nguyen Van B', 3, TRUE, TRUE);

INSERT INTO admin (admin_id) VALUES (1);

INSERT INTO teacher (teacher_id, subject_specialization, qualification, hire_date)
VALUES (2, 'Web Development', 'Master', '2023-01-01');

INSERT INTO student (student_id, class_name) VALUES
(3, 'CNTT01'),
(4, 'CNTT02');

INSERT INTO course_category (category_name, description) VALUES
('Lập trình', 'Khóa học lập trình'),
('Thiết kế', 'Khóa học thiết kế'),
('Marketing', 'Khóa học marketing');

INSERT INTO course
(title, description, category_id, price, duration, level, is_published, published_at)
VALUES
('Java Spring Boot', 'Khóa học Spring Boot cơ bản', 1, 0, 300, 'Beginner', TRUE, NOW()),
('ReactJS', 'Khóa học ReactJS', 1, 0, 250, 'Beginner', TRUE, NOW()),
('UI/UX Design', 'Khóa học thiết kế UI/UX', 2, 0, 200, 'Beginner', TRUE, NOW());

INSERT INTO module (course_id, title, position, is_free) VALUES
(1, 'Giới thiệu Spring Boot', 1, TRUE),
(1, 'REST API', 2, FALSE),
(2, 'React Basics', 1, TRUE),
(3, 'Design Principles', 1, TRUE);

INSERT INTO lesson (module_id, title, duration, position, is_free) VALUES
(1, 'Spring Boot là gì', 20, 1, TRUE),
(1, 'Cấu trúc project', 25, 2, TRUE),
(2, 'Tạo REST API', 30, 1, FALSE),
(3, 'Component & JSX', 35, 1, TRUE),
(4, 'Nguyên tắc UI', 40, 1, TRUE);

INSERT INTO video (lesson_id, title, video_url, duration) VALUES
(1, 'Intro Spring Boot', 'https://video.com/sb1', 900),
(2, 'Project Structure', 'https://video.com/sb2', 1200),
(4, 'React JSX', 'https://video.com/react1', 1500);

INSERT INTO enrollment
(course_id, student_id, payment_status, payment_amount)
VALUES
(1, 3, 'COMPLETED', 0),
(2, 3, 'COMPLETED', 0),
(3, 4, 'COMPLETED', 0);

INSERT INTO progress
(enrollment_id, lesson_id, is_completed)
VALUES
(1, 1, TRUE),
(1, 2, TRUE),
(2, 4, TRUE),
(3, 5, TRUE);

INSERT INTO review
(course_id, student_id, rating, comment)
VALUES
(1, 3, 5, 'Khóa học rất hay'),
(2, 3, 4, 'Dễ hiểu'),
(3, 4, 5, 'Thiết kế đẹp');

INSERT INTO quiz (lesson_id, title, time_limit, passing_score)
VALUES (3, 'Quiz REST API', 20, 70);

INSERT INTO quiz_question (quiz_id, question_text, question_type)
VALUES (1, 'REST là gì?', 'essay');

INSERT INTO quiz_answer (question_id, answer_text, is_correct)
VALUES (1, 'Representational State Transfer', TRUE);

INSERT INTO quiz_attempt
(quiz_id, student_id, score, start_time, submit_time, status)
VALUES (1, 3, 80, NOW(), NOW(), 'PASSED');

INSERT INTO student_answer
(attempt_id, question_id, text_answer, is_correct, points_earned)
VALUES (1, 1, 'REST là kiểu kiến trúc API', TRUE, 10);

INSERT INTO certificate
(enrollment_id, certificate_code)
VALUES (1, 'CERT-SPRING-001');

INSERT INTO notification
(account_id, title, content, notification_type)
VALUES
(3, 'Hoàn thành khóa học', 'Bạn đã hoàn thành Spring Boot', 'course_completion'),
(3, 'Chứng chỉ', 'Bạn đã nhận chứng chỉ', 'certificate');


