document.addEventListener('DOMContentLoaded', function() {
    // Constants & Variables
    const courseId = new URLSearchParams(window.location.search).get('id');
    let courseData = null;
    let enrollmentStatus = false;

    // DOM Elements
    const courseContainer = document.getElementById('course-container');
    const enrollButton = document.getElementById('enroll-button');
    const lessonsList = document.getElementById('lessons-list');
    const ratingContainer = document.getElementById('rating-container');
    const reviewsContainer = document.getElementById('reviews-container');

    // Initialize
    init();

    // Main initialization function
    async function init() {
        if (!courseId) {
            showError('Không tìm thấy khóa học');
            return;
        }

        try {
            await loadCourseDetails();
            await checkEnrollmentStatus();
            setupEventListeners();
        } catch (error) {
            console.error('Error initializing course page:', error);
            showError('Có lỗi xảy ra khi tải thông tin khóa học');
        }
    }

    // Load course details from API
    async function loadCourseDetails() {
        try {
            const response = await api.get(`/api/courses/${courseId}`);
            courseData = response.data;
            renderCourseDetails();
            loadCourseLessons();
            loadCourseReviews();
        } catch (error) {
            console.error('Error loading course details:', error);
            throw error;
        }
    }

    // Check if user is enrolled in this course
    async function checkEnrollmentStatus() {
        try {
            const response = await api.get(`/api/enrollments/check/${courseId}`);
            enrollmentStatus = response.data.enrolled;
            updateEnrollButton();
        } catch (error) {
            console.error('Error checking enrollment status:', error);
            // Continue without enrollment info
        }
    }

    // Render course details to the page
    function renderCourseDetails() {
        if (!courseData) return;

        // Course header section
        document.getElementById('course-title').textContent = courseData.title;
        document.getElementById('course-description').textContent = courseData.description;
        document.getElementById('course-price').textContent = formatCurrency(courseData.price);
        document.getElementById('course-instructor').textContent = courseData.instructor.name;
        document.getElementById('course-category').textContent = courseData.category.name;
        document.getElementById('course-duration').textContent = `${courseData.durationHours} giờ`;
        document.getElementById('course-level').textContent = formatLevel(courseData.level);

        // Course image
        if (courseData.imageUrl) {
            document.getElementById('course-image').src = courseData.imageUrl;
            document.getElementById('course-image').alt = courseData.title;
        }

        // Course rating
        const ratingValue = courseData.averageRating || 0;
        renderStarRating(ratingContainer, ratingValue);
        document.getElementById('rating-value').textContent = ratingValue.toFixed(1);
        document.getElementById('rating-count').textContent = `(${courseData.reviewCount || 0} đánh giá)`;

        // Show the course container after data is loaded
        courseContainer.classList.remove('d-none');
    }

    // Load lessons of the course
    async function loadCourseLessons() {
        try {
            const response = await api.get(`/api/courses/${courseId}/lessons`);
            renderLessons(response.data);
        } catch (error) {
            console.error('Error loading course lessons:', error);
            document.getElementById('lessons-error').classList.remove('d-none');
        }
    }

    // Render lessons to the page
    function renderLessons(lessons) {
        if (!lessons || !lessons.length) {
            document.getElementById('no-lessons').classList.remove('d-none');
            return;
        }

        lessonsList.innerHTML = '';
        lessons.forEach((lesson, index) => {
            const lessonItem = document.createElement('div');
            lessonItem.className = 'lesson-item p-3 border-bottom';

            const isLocked = !enrollmentStatus && !lesson.isFree;

            lessonItem.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h5 class="mb-1">
                            <span class="lesson-number me-2">${index + 1}.</span>
                            ${lesson.title}
                            ${lesson.isFree ? '<span class="badge bg-success ms-2">Miễn phí</span>' : ''}
                        </h5>
                        <p class="text-muted mb-0"><i class="far fa-clock me-1"></i> ${lesson.durationMinutes} phút</p>
                    </div>
                    <div>
                        ${isLocked ?
                '<i class="fas fa-lock text-secondary"></i>' :
                '<button class="btn btn-sm btn-outline-primary watch-lesson" data-id="' + lesson.id + '">Xem bài học</button>'}
                    </div>
                </div>
            `;

            lessonsList.appendChild(lessonItem);
        });

        // Add event listeners to watch buttons
        document.querySelectorAll('.watch-lesson').forEach(button => {
            button.addEventListener('click', function() {
                const lessonId = this.getAttribute('data-id');
                window.location.href = `/lessons/watch?id=${lessonId}&courseId=${courseId}`;
            });
        });

        document.getElementById('lessons-section').classList.remove('d-none');
    }

    // Load course reviews
    async function loadCourseReviews() {
        try {
            const response = await api.get(`/api/courses/${courseId}/reviews`);
            renderReviews(response.data);
        } catch (error) {
            console.error('Error loading course reviews:', error);
            document.getElementById('reviews-error').classList.remove('d-none');
        }
    }

    // Render reviews to the page
    function renderReviews(reviews) {
        if (!reviews || !reviews.length) {
            document.getElementById('no-reviews').classList.remove('d-none');
            return;
        }

        reviewsContainer.innerHTML = '';
        reviews.forEach(review => {
            const reviewItem = document.createElement('div');
            reviewItem.className = 'review-item p-3 border-bottom';

            reviewItem.innerHTML = `
                <div class="d-flex mb-2">
                    <img src="${review.user.avatarUrl || '/images/default-avatar.png'}" 
                         alt="${review.user.fullName}" 
                         class="avatar-sm rounded-circle me-2">
                    <div>
                        <h6 class="mb-0">${review.user.fullName}</h6>
                        <small class="text-muted">${formatDate(review.createdAt)}</small>
                    </div>
                </div>
                <div class="mb-2 rating-stars">
                    ${generateStarRating(review.rating)}
                </div>
                <p class="mb-0">${review.comment}</p>
            `;

            reviewsContainer.appendChild(reviewItem);
        });

        document.getElementById('reviews-section').classList.remove('d-none');
    }

    // Update enrollment button based on status
    function updateEnrollButton() {
        if (!enrollButton) return;

        if (enrollmentStatus) {
            enrollButton.textContent = 'Đã đăng ký';
            enrollButton.classList.remove('btn-primary');
            enrollButton.classList.add('btn-success');
            enrollButton.disabled = true;
        } else {
            enrollButton.textContent = 'Đăng ký khóa học';
            enrollButton.classList.remove('btn-success');
            enrollButton.classList.add('btn-primary');
            enrollButton.disabled = false;
        }
    }

    // Setup event listeners
    function setupEventListeners() {
        // Enroll button click
        if (enrollButton) {
            enrollButton.addEventListener('click', handleEnrollment);
        }

        // Submit review form
        const reviewForm = document.getElementById('review-form');
        if (reviewForm) {
            reviewForm.addEventListener('submit', submitReview);
        }
    }

    // Handle course enrollment
    async function handleEnrollment(event) {
        event.preventDefault();

        if (enrollmentStatus) return;

        try {
            enrollButton.disabled = true;
            enrollButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';

            const response = await api.post(`/api/enrollments`, {
                courseId: courseId
            });

            if (response.status === 200 || response.status === 201) {
                enrollmentStatus = true;
                updateEnrollButton();
                showNotification('success', 'Đăng ký khóa học thành công!');

                // Reload lessons to update access
                loadCourseLessons();
            }
        } catch (error) {
            console.error('Error enrolling in course:', error);

            if (error.response && error.response.status === 402) {
                showNotification('error', 'Vui lòng thanh toán để đăng ký khóa học này');
                // Redirect to payment page
                setTimeout(() => {
                    window.location.href = `/payment?courseId=${courseId}`;
                }, 1500);
            } else {
                showNotification('error', 'Có lỗi xảy ra khi đăng ký khóa học');
                updateEnrollButton();
            }
        }
    }

    // Submit a course review
    async function submitReview(event) {
        event.preventDefault();

        const form = event.target;
        const ratingValue = form.querySelector('input[name="rating"]:checked').value;
        const comment = form.querySelector('textarea[name="comment"]').value;

        try {
            const submitButton = form.querySelector('button[type="submit"]');
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang gửi...';

            const response = await api.post(`/api/courses/${courseId}/reviews`, {
                rating: parseInt(ratingValue),
                comment: comment
            });

            if (response.status === 200 || response.status === 201) {
                showNotification('success', 'Cảm ơn bạn đã đánh giá khóa học!');
                form.reset();

                // Reload reviews and course data
                loadCourseDetails();
            }
        } catch (error) {
            console.error('Error submitting review:', error);
            showNotification('error', 'Có lỗi xảy ra khi gửi đánh giá');
        } finally {
            const submitButton = form.querySelector('button[type="submit"]');
            submitButton.disabled = false;
            submitButton.textContent = 'Gửi đánh giá';
        }
    }

    // Utility: Show error message
    function showError(message) {
        const errorContainer = document.getElementById('error-container');
        if (errorContainer) {
            errorContainer.textContent = message;
            errorContainer.classList.remove('d-none');
        }
    }

    // Utility: Format currency
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    // Utility: Format level
    function formatLevel(level) {
        const levels = {
            'BEGINNER': 'Người mới bắt đầu',
            'INTERMEDIATE': 'Trung cấp',
            'ADVANCED': 'Nâng cao',
            'ALL_LEVELS': 'Tất cả trình độ'
        };
        return levels[level] || level;
    }

    // Utility: Format date
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    // Utility: Render star rating
    function renderStarRating(container, rating) {
        if (!container) return;

        container.innerHTML = generateStarRating(rating);
    }

    // Utility: Generate star rating HTML
    function generateStarRating(rating) {
        const fullStars = Math.floor(rating);
        const halfStar = rating % 1 >= 0.5;
        const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

        let starsHtml = '';

        // Full stars
        for (let i = 0; i < fullStars; i++) {
            starsHtml += '<i class="fas fa-star text-warning"></i>';
        }

        // Half star
        if (halfStar) {
            starsHtml += '<i class="fas fa-star-half-alt text-warning"></i>';
        }

        // Empty stars
        for (let i = 0; i < emptyStars; i++) {
            starsHtml += '<i class="far fa-star text-warning"></i>';
        }

        return starsHtml;
    }
});