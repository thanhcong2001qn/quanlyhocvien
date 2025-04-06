INSERT INTO course_categories (category_name, description)
VALUES ('Lập trình web', 'Các khóa học về phát triển website và ứng dụng web'),
       ('Lập trình di động', 'Các khóa học về phát triển ứng dụng di động'),
       ('Khoa học dữ liệu', 'Các khóa học về phân tích và xử lý dữ liệu'),
       ('Thiết kế đồ họa', 'Các khóa học về thiết kế và đồ họa'),
       ('Marketing số', 'Các khóa học về tiếp thị số và quảng cáo trực tuyến');
INSERT INTO courses (course_code, course_name, description, category_id, teacher_id, max_students, price, start_date,
                     end_date, image_path, status)
VALUES ('WEB001', 'Lập trình Web với HTML, CSS và JavaScript', 'Khóa học cơ bản về lập trình web front-end từ đầu', 1,
        1, 30, 1500000, '2025-05-01', '2025-07-31', '/images/courses/web001.jpg', 'published'),
       ('WEB002', 'React.js cho người mới bắt đầu', 'Học cách xây dựng ứng dụng web với React.js', 1, 1, 25, 2000000,
        '2025-05-15', '2025-08-15', '/images/courses/web002.jpg', 'published'),
       ('MOB001', 'Phát triển ứng dụng Android với Kotlin', 'Học cách xây dựng ứng dụng Android hiện đại với Kotlin', 2,
        2, 20, 2500000, '2025-06-01', '2025-09-01', '/images/courses/mob001.jpg', 'published'),
       ('MOB002', 'Phát triển ứng dụng iOS với Swift', 'Từ cơ bản đến nâng cao về lập trình iOS', 2, 2, 20, 2800000,
        '2025-06-15', '2025-09-15', '/images/courses/mob002.jpg', 'draft'),
       ('DS001', 'Python cho Khoa học dữ liệu', 'Khóa học cơ bản về Python và các thư viện phân tích dữ liệu', 3, 3, 25,
        1800000, '2025-07-01', '2025-10-01', '/images/courses/ds001.jpg', 'published'),
       ('GD001', 'Adobe Photoshop cơ bản đến nâng cao', 'Học chỉnh sửa ảnh và thiết kế đồ họa với Photoshop', 4, 4, 15,
        1200000, '2025-05-10', '2025-07-10', '/images/courses/gd001.jpg', 'published'),
       ('MK001', 'Digital Marketing tổng quan', 'Khóa học về các kỹ thuật marketing trong thời đại số', 5, 5, 30,
        1600000, '2025-05-20', '2025-08-20', '/images/courses/mk001.jpg', 'published');
-- Thêm dữ liệu vào bảng đăng ký khóa học (course_enrollments)
-- Giả sử chúng ta đã có thông tin học viên (student) trong hệ thống
INSERT INTO course_enrollments (course_id, student_id, enrollment_date, status)
VALUES (1, 1, '2025-04-10', 'active'),
       (1, 2, '2025-04-11', 'active'),
       (1, 3, '2025-04-12', 'active'),
       (2, 1, '2025-04-13', 'active'),
       (2, 4, '2025-04-14', 'active'),
       (3, 2, '2025-04-15', 'active'),
       (3, 5, '2025-04-16', 'active'),
       (5, 3, '2025-04-17', 'active'),
       (5, 4, '2025-04-18', 'active'),
       (6, 5, '2025-04-19', 'active'),
       (7, 1, '2025-04-20', 'pending');

-- Thêm dữ liệu vào bảng nội dung khóa học (course_modules)
INSERT INTO course_modules (course_id, module_name, description, order_index)
VALUES (1, 'Giới thiệu HTML', 'Các khái niệm cơ bản về HTML và cấu trúc trang web', 1),
       (1, 'CSS cơ bản', 'Học cách tạo kiểu cho trang web với CSS', 2),
       (1, 'JavaScript cơ bản', 'Tương tác với trang web sử dụng JavaScript', 3),
       (2, 'Giới thiệu React', 'Khái niệm cơ bản về React và JSX', 1),
       (2, 'React Components', 'Học cách tạo và quản lý components trong React', 2),
       (2, 'React Hooks', 'Sử dụng Hooks để quản lý state và side effects', 3),
       (3, 'Cơ bản về Kotlin', 'Giới thiệu về ngôn ngữ Kotlin và cú pháp cơ bản', 1),
       (3, 'Giao diện người dùng Android', 'Xây dựng giao diện người dùng với Android Studio', 2),
       (3, 'Xử lý dữ liệu trong Android', 'Lưu trữ và truy xuất dữ liệu trong ứng dụng Android', 3),
       (5, 'Cơ bản về Python', 'Giới thiệu về ngôn ngữ Python và cú pháp cơ bản', 1),
       (5, 'Thư viện NumPy và Pandas', 'Xử lý dữ liệu với NumPy và Pandas', 2),
       (5, 'Trực quan hóa dữ liệu', 'Biểu diễn dữ liệu bằng Matplotlib và Seaborn', 3);

-- Thêm dữ liệu vào bảng bài học (lessons)
INSERT INTO lessons (module_id, lesson_name, content, video_path, duration_minutes, order_index)
VALUES (1, 'Cấu trúc HTML cơ bản', 'Bài học về cấu trúc cơ bản của một trang HTML', '/videos/lessons/html_basic.mp4',
        45, 1),
       (1, 'Các thẻ HTML phổ biến', 'Giới thiệu các thẻ HTML thường dùng và cách sử dụng',
        '/videos/lessons/html_tags.mp4', 50, 2),
       (1, 'HTML Forms', 'Tạo biểu mẫu và thu thập dữ liệu từ người dùng', '/videos/lessons/html_forms.mp4', 55, 3),
       (2, 'CSS Selectors', 'Cách chọn và áp dụng kiểu cho các phần tử HTML', '/videos/lessons/css_selectors.mp4', 40,
        1),
       (2, 'CSS Box Model', 'Hiểu về mô hình hộp trong CSS', '/videos/lessons/css_box_model.mp4', 35, 2),
       (2, 'CSS Flexbox', 'Bố cục trang web với Flexbox', '/videos/lessons/css_flexbox.mp4', 60, 3),
       (3, 'JavaScript Variables', 'Biến và kiểu dữ liệu trong JavaScript', '/videos/lessons/js_variables.mp4', 30, 1),
       (3, 'JavaScript Functions', 'Tạo và sử dụng hàm trong JavaScript', '/videos/lessons/js_functions.mp4', 45, 2),
       (3, 'DOM Manipulation', 'Thao tác với DOM sử dụng JavaScript', '/videos/lessons/js_dom.mp4', 55, 3),
       (4, 'Giới thiệu về React', 'Lịch sử và các khái niệm cơ bản của React', '/videos/lessons/react_intro.mp4', 40,
        1),
       (4, 'Cài đặt môi trường React', 'Thiết lập môi trường phát triển React', '/videos/lessons/react_setup.mp4', 30,
        2),
       (4, 'JSX Syntax', 'Tìm hiểu về cú pháp JSX trong React', '/videos/lessons/react_jsx.mp4', 45, 3);

-- Thêm dữ liệu vào bảng đánh giá khóa học (course_reviews)
INSERT INTO course_reviews (course_id, student_id, rating, comment)
VALUES (1, 1, 5, 'Khóa học rất hay và dễ hiểu cho người mới bắt đầu'),
       (1, 2, 4, 'Nội dung đầy đủ, giảng viên nhiệt tình'),
       (2, 1, 5, 'React rất thú vị, tôi đã học được nhiều điều mới'),
       (3, 2, 4, 'Khóa học Kotlin rất hữu ích, giúp tôi phát triển ứng dụng Android đầu tiên'),
       (5, 3, 5, 'Khóa học Python giúp tôi hiểu rõ về khoa học dữ liệu'),
       (5, 4, 4, 'Nội dung phong phú, nhiều ví dụ thực tế'),
       (6, 5, 3, 'Khóa học Photoshop khá tốt, nhưng cần thêm bài tập thực hành');

-- Thêm dữ liệu vào bảng bài tập (assignments)
INSERT INTO assignments (course_id, title, description, due_date, max_score)
VALUES (1, 'Xây dựng trang portfolio cá nhân', 'Sử dụng HTML và CSS để tạo trang web portfolio cá nhân đơn giản',
        '2025-06-01 23:59:59', 100),
       (1, 'Tạo ứng dụng Todo List', 'Xây dựng ứng dụng Todo List đơn giản sử dụng HTML, CSS và JavaScript',
        '2025-06-15 23:59:59', 100),
       (2, 'Component React cơ bản', 'Tạo 3 components React độc lập và kết hợp chúng', '2025-06-10 23:59:59', 100),
       (2, 'Ứng dụng Shopping Cart', 'Xây dựng ứng dụng giỏ hàng đơn giản với React', '2025-07-01 23:59:59', 100),
       (3, 'Ứng dụng Calculator', 'Xây dựng ứng dụng máy tính đơn giản cho Android', '2025-07-10 23:59:59', 100),
       (5, 'Phân tích dữ liệu', 'Sử dụng Python để phân tích bộ dữ liệu được cung cấp', '2025-08-01 23:59:59', 100);

-- Thêm dữ liệu vào bảng nộp bài tập (assignment_submissions)
INSERT INTO assignment_submissions (assignment_id, student_id, submission_text, file_path, submitted_at, score,
                                    feedback, graded_by, graded_at)
VALUES (1, 1, 'Em đã hoàn thành bài tập xây dựng trang portfolio cá nhân với các yêu cầu đầy đủ.',
        '/submissions/student1_portfolio.zip', '2025-05-28 20:15:30', 90,
        'Bài làm tốt, có sáng tạo. Cần cải thiện phần responsive.', 1, '2025-06-02 10:30:00'),
       (1, 2, 'Em đã tạo trang portfolio với đầy đủ các mục yêu cầu.', '/submissions/student2_portfolio.zip',
        '2025-05-30 18:20:15', 85, 'Bài làm đầy đủ, cần cải thiện về mặt thiết kế.', 1, '2025-06-02 11:00:00'),
       (2, 1, 'Em đã xây dựng ứng dụng Todo List có thể thêm, sửa, xóa và đánh dấu hoàn thành các công việc.',
        '/submissions/student1_todolist.zip', '2025-06-14 21:30:45', NULL, NULL, NULL, NULL),
       (3, 1, 'Em đã tạo 3 components: Header, ProductCard và Footer theo yêu cầu.',
        '/submissions/student1_react_components.zip', '2025-06-08 16:45:20', 95,
        'Bài làm xuất sắc, components được tổ chức tốt và có tính tái sử dụng cao.', 1, '2025-06-11 09:15:00');