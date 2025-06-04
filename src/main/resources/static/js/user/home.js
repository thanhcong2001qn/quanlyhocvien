document.addEventListener('DOMContentLoaded', function () {
    const authenticatedPage = document.getElementById('authenticated');
    const unauthenticatedPage = document.getElementById('not-authenticated');
    authenticatedPage.style.display = "none";
    unauthenticatedPage.style.display = "none";
    // Fetch user data and update UI
    fetchWithAuth('/api/verify-token')
        .then(response => {
            if (!response.ok) {
                localStorage.clear();
                unauthenticatedPage.style.display = "block";
                //return Promise.reject('Authentication failed');
                document.addEventListener('click', function(event) {
                    // Lấy phần tử được click
                    const target = event.target;

                    // Tìm thẻ a gần nhất (nếu click vào con của thẻ a)
                    const linkElement = target.closest('a');

                    if (linkElement) {
                        const href = linkElement.getAttribute('href');

                        // Cho phép truy cập các liên kết đăng nhập và đăng ký
                        if (href === '/login' || href === '/register' ||
                            href.startsWith('/css') || href.startsWith('/js') ||
                            href.startsWith('/images') || href === '#') {
                            return; // Cho phép truy cập các liên kết này
                        }

                        // Ngăn chặn hành vi mặc định của liên kết
                        event.preventDefault();

                        // Lưu URL người dùng đang cố truy cập (để chuyển hướng sau khi đăng nhập)
                        if (href && href !== '#' && !href.startsWith('javascript:')) {
                            localStorage.setItem('redirectAfterLogin', href);
                        }

                        // Chuyển hướng đến trang đăng nhập
                        window.location.href = '/login';
                    }
                });

            }
            else if (response.ok){
                loadDashboardStats();
                loadPopularCourses();
                authenticatedPage.style.display = "block";
            }
            return response.json();
        })
        .then(data => {
            localStorage.setItem('username', data.username);
            localStorage.setItem('isAuthenticated', 'true');

            // Update username in the welcome section
            const usernameElement = document.getElementById('username');
            if (usernameElement && data.username) {
                usernameElement.textContent = data.username;
            }

            // Load user dashboard data
            return fetchWithAuth('/api/user/dashboard');
        })
        .then(response => {
            if (!response.ok) return Promise.reject('Failed to load dashboard data');
            return response.json();
        })
        .then(dashboard => {
            // Update dashboard with real data
            console.log('Dashboard data loaded:', dashboard);
            // TODO: Update UI with actual data
        })
        .catch(error => {
            console.error('Error:', error);
        });

    // Animation for learning path
    const pathItems = document.querySelectorAll('.path-item');
    pathItems.forEach((item, index) => {
        setTimeout(() => {
            item.classList.add('fade-in');
        }, index * 200);
    });
});

function loadPopularCourses() {
    const container = document.getElementById('popular-courses-container');
    const loadingElement = document.getElementById('popular-courses-loading');
    const errorElement = document.getElementById('popular-courses-error');

    // Hiển thị loading, ẩn error và container
    loadingElement.style.display = 'block';
    errorElement.classList.add('d-none');
    container.innerHTML = '';

    // Gọi API để lấy 3 khóa học phổ biến nhất
    fetchWithAuth('/api/user/popular?size=3')
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải khóa học phổ biến');
            }
            return response.json();
        })
        .then(courses => {
            // Ẩn loading
            loadingElement.style.display = 'none';

            // Kiểm tra nếu không có khóa học
            if (!courses || courses.length === 0) {
                container.innerHTML = '<div class="text-center py-4">Chưa có khóa học nào.</div>';
                return;
            }

            // Hiển thị từng khóa học
            courses.forEach(course => {
                // Tạo badge nếu có nhiều người đăng ký
                let badgeHtml = '';
                if (course.enrollmentCount > 50) {
                    badgeHtml = '<div class="course-badge">Hot</div>';
                } else if (course.isNew) {
                    badgeHtml = '<div class="course-badge">Mới</div>';
                } else if (course.enrollmentCount > 30) {
                    badgeHtml = '<div class="course-badge">Phổ biến</div>';
                }

                // Tạo HTML hiển thị số sao đánh giá
                const ratingHtml = generateRatingStars(course.rating);

                // Tạo HTML cho card khóa học
                const courseCard = `
                    <div class="course-card">
                        <div class="course-image">
                            <img src="${course.thumbnailUrl || 'https://via.placeholder.com/300x180'}" alt="${course.title}">
                            ${badgeHtml}
                        </div>
                        <div class="course-content">
                            <h3 class="course-title">${course.title}</h3>
                            <div class="course-meta">
                                <span><i class="fas fa-book"></i> ${course.lessonCount || 0} bài học</span>
                                <span><i class="fas fa-clock"></i> ${course.totalDuration || 0} giờ</span>
                            </div>
                            <div class="course-enrollment">
                                <i class="fas fa-users text-primary"></i> ${course.enrollmentCount || 0} học viên
                            </div>
                            <div class="course-rating">
                                ${ratingHtml}
                                <span>${course.rating.toFixed(1)} (${course.ratingCount || 0})</span>
                            </div>
                            <div class="course-footer">
                                <a href="/user/course-detail/${course.courseId}" class="btn btn-outline-primary btn-sm">Xem chi tiết</a>
                            </div>
                        </div>
                    </div>
                `;

                // Thêm card vào container
                container.innerHTML += courseCard;
            });
        })
        .catch(error => {
            console.error('Error loading popular courses:', error);
            loadingElement.style.display = 'none';
            errorElement.classList.remove('d-none');
        });
}

/**
 * Tạo HTML hiển thị số sao đánh giá
 */
function generateRatingStars(rating) {
    let html = '';
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;

    // Thêm sao đầy đủ
    for (let i = 0; i < fullStars; i++) {
        html += '<i class="fas fa-star text-warning"></i>';
    }

    // Thêm nửa sao nếu có
    if (hasHalfStar) {
        html += '<i class="fas fa-star-half-alt text-warning"></i>';
    }

    // Thêm sao trống
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    for (let i = 0; i < emptyStars; i++) {
        html += '<i class="far fa-star text-warning"></i>';
    }

    return html;
}
function loadDashboardStats() {
    fetchWithAuth('/api/user/stats')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load user stats');
            }
            return response.json();
        })
        .then(stats => {
            // Hiển thị số khóa học đã đăng ký
            document.getElementById('enrolled-courses-count').textContent = stats.enrolledCoursesCount;

            // Hiển thị số bài học đã hoàn thành
            document.getElementById('completed-lessons-count').textContent = stats.completedLessonsCount;

            // Hiển thị tổng giờ học tập (làm tròn 1 chữ số thập phân)
            document.getElementById('total-learning-hours').textContent = stats.totalLearningHours.toFixed(1);

            // Hiển thị điểm thành tích
            document.getElementById('achievement-points').textContent = stats.achievementPoints;
        })
        .catch(error => {
            console.error('Error loading user stats:', error);

            // Hiển thị dấu gạch ngang nếu không thể tải dữ liệu
            document.getElementById('enrolled-courses-count').textContent = '-';
            document.getElementById('completed-lessons-count').textContent = '-';
            document.getElementById('total-learning-hours').textContent = '-';
            document.getElementById('achievement-points').textContent = '-';
        });
}