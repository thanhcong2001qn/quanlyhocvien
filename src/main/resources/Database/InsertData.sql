-- Thêm dữ liệu vào bảng course_category
INSERT INTO course_category (category_name, description, icon_path)
VALUES ('Lập trình', 'Các khóa học về lập trình, phát triển phần mềm', '/icons/programming.png'),
       ('Thiết kế', 'Các khóa học về thiết kế giao diện, đồ họa', '/icons/design.png'),
       ('Marketing', 'Các khóa học về digital marketing, SEO', '/icons/marketing.png'),
       ('Ngoại ngữ', 'Các khóa học về ngôn ngữ', '/icons/language.png'),
       ('Kỹ năng mềm', 'Các khóa học về kỹ năng giao tiếp, lãnh đạo', '/icons/soft-skills.png');

-- Thêm dữ liệu khóa học miễn phí vào bảng course
INSERT INTO course (title, description, category_id, thumbnail_path, price, discount_price, duration, level,
                    is_published, published_at, is_featured, rating, total_students, total_reviews)
VALUES ('Nhập môn lập trình với Python',
        'Khóa học miễn phí giúp người mới bắt đầu làm quen với lập trình thông qua ngôn ngữ Python đơn giản và dễ học. Bạn sẽ học cách viết code, giải quyết vấn đề và xây dựng các ứng dụng nhỏ.',
        1, '/img/courses/python-intro.jpg', 0, NULL, 240, 'beginner', 1, '2025-01-15 10:00:00', 1, 4.7, 15280, 1245),
       ('HTML & CSS cơ bản',
        'Học cách xây dựng trang web đầu tiên với HTML và CSS. Khóa học cung cấp kiến thức nền tảng về cấu trúc và thiết kế trang web, giúp bạn bắt đầu hành trình trở thành web developer.',
        1, '/img/courses/html-css-basics.jpg', 0, NULL, 180, 'beginner', 1, '2025-02-01 09:30:00', 0, 4.5, 12450, 980),
       ('JavaScript cho người mới bắt đầu',
        'Làm quen với ngôn ngữ lập trình phổ biến nhất thế giới. Khóa học miễn phí này dạy bạn cơ bản về JavaScript, DOM, xử lý sự kiện và cách tạo trang web tương tác.',
        1, '/img/courses/js-beginners.jpg', 0, NULL, 210, 'beginner', 1, '2025-02-20 14:00:00', 1, 4.6, 10320, 876),
       ('Digital Marketing cơ bản',
        'Khám phá thế giới marketing online với khóa học miễn phí toàn diện. Tìm hiểu về SEO, Social Media, Email Marketing và các kỹ thuật marketing số cơ bản để bắt đầu sự nghiệp.',
        3, '/img/courses/digital-marketing.jpg', 0, NULL, 150, 'beginner', 1, '2025-03-05 11:00:00', 1, 4.4, 8760, 743),
       ('SEO căn bản - Tối ưu tìm kiếm',
        'Học cách tối ưu hóa trang web của bạn cho công cụ tìm kiếm. Khóa học miễn phí này giới thiệu các kỹ thuật SEO On-page và Off-page, giúp tăng thứ hạng website.',
        3, '/img/courses/basic-seo.jpg', 0, NULL, 120, 'beginner', 1, '2025-03-10 16:30:00', 0, 4.3, 7540, 621),
       ('Tiếng Anh giao tiếp cơ bản',
        'Khóa học miễn phí dành cho người mới bắt đầu học tiếng Anh. Tập trung vào giao tiếp hàng ngày, phát âm và ngữ pháp cơ bản để giúp bạn tự tin nói tiếng Anh.',
        5, '/img/courses/english-basics.jpg', 0, NULL, 300, 'beginner', 1, '2025-01-20 08:00:00', 1, 4.8, 18920, 1532),
       ('Tiếng Nhật cho người mới bắt đầu',
        'Làm quen với tiếng Nhật qua khóa học miễn phí này. Học bảng chữ cái Hiragana, Katakana, một số Kanji cơ bản và các câu giao tiếp thông dụng trong cuộc sống.',
        5, '/img/courses/japanese-intro.jpg', 0, NULL, 270, 'beginner', 1, '2025-02-10 09:00:00', 0, 4.5, 6320, 521),
       ('Quản lý thời gian hiệu quả',
        'Khóa học miễn phí dạy bạn cách tổ chức và quản lý thời gian một cách hiệu quả. Học cách đặt mục tiêu, ưu tiên công việc và tăng năng suất trong công việc và học tập.',
        4, '/img/courses/time-management.jpg', 0, NULL, 90, 'all-levels', 1, '2025-03-20 10:00:00', 1, 4.6, 9870, 812),
       ('Kỹ năng thuyết trình cơ bản',
        'Học cách thuyết trình tự tin và hiệu quả với khóa học miễn phí này. Khóa học cung cấp các kỹ thuật chuẩn bị, thiết kế slide và trình bày trước đám đông.',
        4, '/img/courses/presentation-skills.jpg', 0, NULL, 120, 'beginner', 1, '2025-04-01 13:30:00', 0, 4.4, 7650,
        634),
       ('Khởi nghiệp từ A-Z',
        'Hướng dẫn miễn phí về cách bắt đầu kinh doanh từ ý tưởng đến hiện thực. Học cách xác định cơ hội, lập kế hoạch kinh doanh và bước đầu khởi nghiệp với chi phí thấp.',
        2, '/img/courses/startup-basics.jpg', 0, NULL, 180, 'beginner', 1, '2025-01-25 15:00:00', 1, 4.7, 11230, 934);
-- Thêm dữ liệu vào bảng module
INSERT INTO module (course_id, title, description, position, is_free)
VALUES (1, 'Giới thiệu về ReactJS', 'Tổng quan về ReactJS và các khái niệm cơ bản', 1, TRUE),
       (1, 'Components và Props', 'Tìm hiểu về components và cách truyền dữ liệu với props', 2, FALSE),
       (1, 'State và Lifecycle', 'Quản lý trạng thái và vòng đời component', 3, FALSE),
       (2, 'Giới thiệu JavaScript', 'Cơ bản về JavaScript và cách hoạt động', 1, TRUE),
       (2, 'Variables, Data Types và Functions', 'Các kiểu dữ liệu và hàm trong JavaScript', 2, FALSE),
       (3, 'Cơ bản về UI/UX', 'Nguyên tắc thiết kế UI/UX', 1, TRUE),
       (3, 'Làm việc với Figma', 'Hướng dẫn sử dụng công cụ Figma', 2, FALSE);

-- Thêm dữ liệu vào bảng lesson
INSERT INTO lesson (module_id, title, description, duration, position, is_free)
VALUES (1, 'ReactJS là gì?', 'Giới thiệu về thư viện ReactJS và lịch sử phát triển', 15, 1, TRUE),
       (1, 'Virtual DOM', 'Hiểu về Virtual DOM và cách ReactJS tối ưu rendering', 20, 2, TRUE),
       (2, 'Functional Components', 'Tạo và sử dụng functional components', 25, 1, FALSE),
       (2, 'Class Components', 'Tạo và sử dụng class components', 30, 2, FALSE),
       (3, 'useState Hook', 'Quản lý state với useState hook', 35, 1, FALSE),
       (4, 'JavaScript và HTML', 'Cách nhúng JavaScript vào trang web', 20, 1, TRUE),
       (4, 'Biến và hằng số', 'Khai báo biến với var, let và const', 25, 2, FALSE),
       (5, 'Functions và Arrow Functions', 'Cú pháp hàm trong JavaScript', 30, 1, FALSE),
       (6, 'Nguyên tắc thiết kế UI/UX', 'Các nguyên tắc cơ bản trong thiết kế giao diện', 40, 1, TRUE),
       (7, 'Giao diện Figma', 'Làm quen với giao diện Figma', 25, 1, FALSE);

-- Thêm dữ liệu vào bảng video
INSERT INTO video (lesson_id, title, video_url, thumbnail_path, duration, is_downloadable)
VALUES (1, 'Giới thiệu ReactJS', 'https://videos.example.com/reactjs-intro.mp4', '/thumbnails/reactjs-intro.jpg', 900,
        FALSE),
       (2, 'Virtual DOM và cơ chế hoạt động', 'https://videos.example.com/virtual-dom.mp4',
        '/thumbnails/virtual-dom.jpg', 1200, FALSE),
       (3, 'Functional Components trong React', 'https://videos.example.com/functional-components.mp4',
        '/thumbnails/functional-components.jpg', 1500, TRUE),
       (4, 'Class Components trong React', 'https://videos.example.com/class-components.mp4',
        '/thumbnails/class-components.jpg', 1800, TRUE),
       (5, 'useState Hook Tutorial', 'https://videos.example.com/usestate-hook.mp4', '/thumbnails/usestate-hook.jpg',
        2100, FALSE),
       (6, 'JavaScript cơ bản', 'https://videos.example.com/js-basics.mp4', '/thumbnails/js-basics.jpg', 1200, FALSE),
       (7, 'Biến trong JavaScript', 'https://videos.example.com/js-variables.mp4', '/thumbnails/js-variables.jpg', 1500,
        FALSE),
       (8, 'Functions trong JavaScript', 'https://videos.example.com/js-functions.mp4', '/thumbnails/js-functions.jpg',
        1800, TRUE),
       (9, 'Nguyên lý thiết kế UI/UX', 'https://videos.example.com/ui-ux-principles.mp4',
        '/thumbnails/ui-ux-principles.jpg', 2400, FALSE),
       (10, 'Hướng dẫn sử dụng Figma', 'https://videos.example.com/figma-tutorial.mp4',
        '/thumbnails/figma-tutorial.jpg', 1500, FALSE);

-- Thêm dữ liệu vào bảng lesson_attachment
INSERT INTO lesson_attachment (lesson_id, title, file_path, file_type, file_size)
VALUES (1, 'Tổng quan về ReactJS', '/attachments/reactjs-overview.pdf', 'PDF', 2048),
       (1, 'Slide bài giảng', '/attachments/reactjs-slides.pptx', 'PPTX', 5120),
       (3, 'Code ví dụ', '/attachments/functional-components-example.zip', 'ZIP', 1024),
       (6, 'Tài liệu JavaScript', '/attachments/javascript-docs.pdf', 'PDF', 3072),
       (9, 'Nguyên tắc thiết kế UI/UX', '/attachments/ui-ux-principles.pdf', 'PDF', 4096);

-- Thêm dữ liệu vào bảng enrollment
INSERT INTO enrollment (course_id, student_id, enrollment_date, payment_status, payment_amount, payment_method,
                        transaction_id, payment_date)
VALUES (1, 20, '2025-03-01 14:30:00', 'completed', 1200000, 'credit_card', 'TXN123456789', '2025-03-01 14:35:22'),
       (2, 17, '2025-03-05 10:15:00', 'completed', 990000, 'momo', 'TXN123456790', '2025-03-05 10:20:45'),
       (3, 18, '2025-02-20 09:45:00', 'completed', 1500000, 'bank_transfer', 'TXN123456791', '2025-02-20 11:30:18'),
       (1, 19, '2025-03-10 16:20:00', 'completed', 1200000, 'paypal', 'TXN123456792', '2025-03-10 16:25:30'),
       (4, 21, '2025-04-01 13:10:00', 'completed', 1800000, 'credit_card', 'TXN123456793', '2025-04-01 13:15:42');

-- Thêm dữ liệu vào bảng progress
INSERT INTO progress (enrollment_id, lesson_id, video_position, is_completed, last_accessed_at, completion_date)
VALUES (1, 1, 900, TRUE, '2025-03-02 10:45:22', '2025-03-02 10:45:22'),
       (1, 2, 1200, TRUE, '2025-03-02 11:15:30', '2025-03-02 11:15:30'),
       (1, 3, 750, FALSE, '2025-03-10 14:20:15', NULL),
       (2, 6, 1200, TRUE, '2025-03-07 09:30:45', '2025-03-07 09:30:45'),
       (2, 7, 800, FALSE, '2025-03-12 20:15:10', NULL),
       (3, 9, 2400, TRUE, '2025-03-01 15:45:22', '2025-03-01 15:45:22'),
       (3, 10, 900, FALSE, '2025-03-15 18:20:30', NULL),
       (4, 1, 900, TRUE, '2025-03-12 21:10:05', '2025-03-12 21:10:05'),
       (4, 2, 600, FALSE, '2025-03-15 22:05:18', NULL);

-- Thêm dữ liệu vào bảng review
INSERT INTO review (course_id, student_id, rating, comment, created_at)
VALUES (1, 4, 5, 'Khóa học rất hay và dễ hiểu. Giảng viên truyền đạt tốt!', '2025-03-15 16:30:45'),
       (1, 6, 4, 'Nội dung chất lượng, cần bổ sung thêm bài tập thực hành', '2025-03-20 09:15:22'),
       (2, 4, 5, 'Khóa học giúp tôi hiểu rõ về JavaScript, rất hữu ích!', '2025-03-25 14:40:10'),
       (3, 5, 4, 'Nội dung bài giảng tốt, nhưng cần cập nhật một số công cụ mới hơn', '2025-03-10 11:25:30'),
       (4, 5, 5, 'Kiến thức thực tế, áp dụng được ngay vào công việc', '2025-04-10 10:05:18');

-- Thêm dữ liệu vào bảng quiz
INSERT INTO quiz (lesson_id, title, description, time_limit, passing_score, attempts_allowed)
VALUES (3, 'Kiểm tra Functional Components', 'Bài kiểm tra về functional components trong ReactJS', 30, 70, 2),
       (5, 'Kiểm tra useState Hook', 'Bài kiểm tra về useState hook trong ReactJS', 20, 80, 2),
       (7, 'Kiểm tra Biến trong JavaScript', 'Bài kiểm tra về các loại biến trong JavaScript', 15, 75, 3),
       (8, 'Kiểm tra Functions', 'Bài kiểm tra về functions trong JavaScript', 25, 70, 2),
       (9, 'Kiểm tra nguyên tắc UI/UX', 'Bài kiểm tra về các nguyên tắc thiết kế UI/UX', 30, 60, 3);

-- Thêm dữ liệu vào bảng quiz_question
INSERT INTO quiz_question (quiz_id, question_text, question_type, points, position)
VALUES (1, 'Đâu là cách khởi tạo functional component?', 'multiple_choice', 2, 1),
       (1, 'Props có thể thay đổi trong functional component?', 'true_false', 1, 2),
       (1, 'Viết một functional component hiển thị danh sách người dùng', 'essay', 5, 3),
       (2, 'useState hook trả về những giá trị gì?', 'multiple_choice', 2, 1),
       (2, 'useState có thể sử dụng trong class component?', 'true_false', 1, 2),
       (3, 'Sự khác biệt giữa let và const là gì?', 'multiple_choice', 2, 1),
       (3, 'var có phạm vi block scope?', 'true_false', 1, 2),
       (4, 'Đâu là cách khai báo arrow function?', 'multiple_choice', 2, 1),
       (4, 'Viết một hàm tính tổng các số trong một mảng', 'essay', 3, 2),
       (5, 'Nguyên tắc nào không phải là nguyên tắc thiết kế UI?', 'multiple_choice', 2, 1);

-- Thêm dữ liệu vào bảng quiz_answer
INSERT INTO quiz_answer (question_id, answer_text, is_correct, position)
VALUES (1, 'function MyComponent() { return <div></div>; }', TRUE, 1),
       (1, 'class MyComponent { render() { return <div></div>; } }', FALSE, 2),
       (1, 'const MyComponent = () => <div></div>;', TRUE, 3),
       (1, 'var MyComponent = function() { <div></div> }', FALSE, 4),
       (2, 'TRUE', FALSE, 1),
       (2, 'FALSE', TRUE, 2),
       (4, '[state, setState]', TRUE, 1),
       (4, 'state', FALSE, 2),
       (4, 'setState', FALSE, 3),
       (4, '[value, setValue]', TRUE, 4),
       (5, 'TRUE', FALSE, 1),
       (5, 'FALSE', TRUE, 2),
       (6, 'let có thể gán lại giá trị, const không thể', TRUE, 1),
       (6, 'let có block scope, const không có', FALSE, 2),
       (6, 'const phải khởi tạo giá trị khi khai báo, let không cần', TRUE, 3),
       (6, 'let dùng cho biến số, const dùng cho chuỗi', FALSE, 4),
       (7, 'TRUE', FALSE, 1),
       (7, 'FALSE', TRUE, 2),
       (8, '() => {}', TRUE, 1),
       (8, 'function() {}', FALSE, 2),
       (8, 'const func = () => {}', TRUE, 3),
       (8, 'function => {}', FALSE, 4),
       (10, 'Balance', FALSE, 1),
       (10, 'Contrast', FALSE, 2),
       (10, 'Programming Logic', TRUE, 3),
       (10, 'Hierarchy', FALSE, 4);

-- Thêm dữ liệu vào bảng quiz_attempt
INSERT INTO quiz_attempt (quiz_id, student_id, score, start_time, submit_time, time_spent, status)
VALUES (1, 4, 80, '2025-03-15 15:00:00', '2025-03-15 15:25:30', 1530, 'passed'),
       (2, 4, 75, '2025-03-16 14:30:00', '2025-03-16 14:48:45', 1125, 'passed'),
       (3, 4, 60, '2025-03-25 10:00:00', '2025-03-25 10:13:20', 800, 'failed'),
       (3, 4, 85, '2025-03-26 11:15:00', '2025-03-26 11:29:10', 850, 'passed'),
       (5, 5, 70, '2025-03-12 16:45:00', '2025-03-12 17:10:22', 1522, 'passed');

-- Thêm dữ liệu vào bảng student_answer
INSERT INTO student_answer (attempt_id, question_id, answer_id, text_answer, is_correct, points_earned)
VALUES (1, 1, 1, NULL, TRUE, 2),
       (1, 2, 5, NULL, FALSE, 0),
       (1, 3, NULL,
        'function UserList({ users }) { return ( <div> <h2>User List</h2> <ul> {users.map(user => ( <li key={user.id}>{user.name}</li> ))} </ul> </div> ); }',
        NULL, 4),
       (2, 4, 4, NULL, TRUE, 2),
       (2, 5, 5, NULL, FALSE, 0),
       (3, 6, 2, NULL, FALSE, 0),
       (3, 7, 8, NULL, TRUE, 1),
       (4, 6, 1, NULL, TRUE, 2),
       (4, 7, 8, NULL, TRUE, 1),
       (5, 10, 10, NULL, FALSE, 0);

-- Thêm dữ liệu vào bảng certificate
INSERT INTO certificate (enrollment_id, certificate_code, issue_date, certificate_path)
VALUES (1, 'CERT-REACT-2025-001', '2025-04-01 10:30:00', '/certificates/react-001.pdf'),
       (3, 'CERT-FIGMA-2025-001', '2025-03-25 14:15:00', '/certificates/figma-001.pdf');

-- Thêm dữ liệu vào bảng notification
INSERT INTO notification (account_id, title, content, is_read, notification_type, reference_id)
VALUES (4, 'Hoàn thành khóa học ReactJS', 'Chúc mừng! Bạn đã hoàn thành khóa học "Lập trình Web với ReactJS"', FALSE,
        'course_completion', 1),
       (4, 'Nhắc nhở làm bài kiểm tra', 'Bạn có một bài kiểm tra sắp hết hạn trong khóa học JavaScript', FALSE,
        'quiz_reminder', 3),
       (5, 'Chứng chỉ mới', 'Chứng chỉ khóa học "Thiết kế UI/UX với Figma" của bạn đã được cấp', TRUE,
        'certificate_issued', 3),
       (6, 'Cập nhật khóa học', 'Khóa học "Lập trình Web với ReactJS" vừa được cập nhật nội dung mới', FALSE,
        'course_update', 1),
       (5, 'Khuyến mãi đặc biệt', 'Giảm 30% cho tất cả khóa học mới trong tháng này', TRUE, 'promotion', NULL);