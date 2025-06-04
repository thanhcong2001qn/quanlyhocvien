/**
 * Course Detail JavaScript File
 * Created by: thanhcong2001qncode
 * Last Updated: 2025-04-24
 */

document.addEventListener('DOMContentLoaded', function () {
    const courseId = document.getElementById('enrollBtn')?.dataset.courseId ||
        document.getElementById('addToCartBtn')?.dataset.courseId;

    // Nếu không tìm thấy courseId, không cần thực hiện các chức năng liên quan đến khóa học
    if (!courseId) {
        console.warn('Course ID not found on page');
        return;
    }
    updateBackButtonUrl();
    initializePreviewVideo();
    initializeCourseActions();
    initializeReviewsLoader();
    initializeSocialSharing();
});

/**
 * Khởi tạo xem trước video cho các bài học miễn phí
 */
function initializePreviewVideo() {
    const playButton = document.querySelector('.play-button');
    if (playButton) {
        playButton.addEventListener('click', function () {
            // Lấy video preview đầu tiên
            const firstFreeLesson = findFirstFreeLesson();
            if (firstFreeLesson) {
                showVideoPreviewModal(firstFreeLesson);
            } else {
                // Không tìm thấy bài học miễn phí
                showNotification('Không có bài học xem trước trong khóa học này!', 'info');
            }
        });
    }

    // Cũng áp dụng cho các bài học có thể xem trước
    document.querySelectorAll('.lecture-item').forEach(item => {
        const previewBadge = item.querySelector('.lecture-preview');
        if (previewBadge) {
            item.addEventListener('click', function () {
                const lessonTitle = item.querySelector('.lecture-title').textContent;
                const lessonId = item.dataset.lessonId;
                showVideoPreviewModal({id: lessonId, title: lessonTitle});
            });
            item.style.cursor = 'pointer';
        }
    });
}

/**
 * Tìm bài học miễn phí đầu tiên
 */
function findFirstFreeLesson() {
    // Tìm bài học có class 'lecture-preview'
    const freePreview = document.querySelector('.lecture-preview');
    if (!freePreview) return null;

    // Lấy thông tin bài học
    const lessonItem = freePreview.closest('.lecture-item');
    const lessonTitle = lessonItem.querySelector('.lecture-title').textContent;
    const lessonId = lessonItem.dataset.lessonId;

    return {
        id: lessonId,
        title: lessonTitle
    };
}

/**
 * Hiển thị modal xem trước video
 */
function showVideoPreviewModal(lesson) {
    // Kiểm tra xem modal đã tồn tại chưa, nếu chưa thì tạo mới
    let videoModal = document.getElementById('videoPreviewModal');
    if (!videoModal) {
        // Tạo modal HTML
        const modalHTML = `
            <div class="modal fade" id="videoPreviewModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="videoPreviewTitle">Xem trước: ${lesson.title}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-0">
                            <div class="ratio ratio-16x9">
                                <iframe id="videoPreviewFrame" src="/api/lessons/preview/${lesson.id}" 
                                        allowfullscreen allow="autoplay; encrypted-media; picture-in-picture"></iframe>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHTML);
        videoModal = document.getElementById('videoPreviewModal');
    } else {
        // Cập nhật thông tin modal
        document.getElementById('videoPreviewTitle').textContent = `Xem trước: ${lesson.title}`;
        document.getElementById('videoPreviewFrame').src = `/api/lessons/preview/${lesson.id}`;
    }

    // Hiển thị modal
    const bsModal = new bootstrap.Modal(videoModal);
    bsModal.show();

    // Xử lý khi đóng modal
    videoModal.addEventListener('hidden.bs.modal', function () {
        document.getElementById('videoPreviewFrame').src = '';
    });
}

/**
 * Khởi tạo các chức năng đăng ký và thêm vào giỏ hàng
 */
function initializeCourseActions() {
    // Xử lý nút đăng ký học
    const enrollBtn = document.getElementById('enrollBtn');
    if (enrollBtn) {
        const courseId = enrollBtn.dataset.courseId;
        const isFree = enrollBtn.dataset.isFree === 'true';
        const coursePrice = parseFloat(enrollBtn.dataset.price || '0');

        // Kiểm tra trạng thái đăng ký khóa học ngay khi trang load
        checkEnrollmentStatus(courseId, enrollBtn);

        enrollBtn.addEventListener('click', function () {
            // Kiểm tra đăng nhập từ localStorage
            const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true';

            if (!isAuthenticated) {
                // Chuyển hướng đến trang đăng nhập
                window.location.href = '/login?redirect=/course-detail/' + courseId;
                return;
            }

            // Hiển thị thông báo đang xử lý
            showNotification('Đang xử lý...', 'info');

            // Kiểm tra xem học viên đã đăng ký khóa học chưa
            fetchWithAuth(`/api/enrollments/check/${courseId}`)
                .then(response => response.json())
                .then(data => {
                    if (data.enrolled) {
                        // Đã đăng ký khóa học
                        showNotification('Bạn đã đăng ký khóa học này rồi!', 'info');
                        markAsEnrolled(enrollBtn);
                        // setTimeout(() => {
                        //     window.location.href = '/learning/' + courseId;
                        // }, 1500);
                    } else {
                        // Chưa đăng ký, kiểm tra xem khóa học là miễn phí hay trả phí
                        if (isFree) {
                            // Khóa học miễn phí, thực hiện đăng ký ngay
                            enrollFreeCourse(courseId, enrollBtn);
                        } else {
                            // Khóa học trả phí, chuyển đến trang thanh toán
                            window.location.href = '/checkout?courseId=' + courseId;
                        }
                    }
                })
                .catch(error => {
                    console.error('Error checking enrollment status:', error);
                    showNotification('Có lỗi xảy ra khi kiểm tra trạng thái đăng ký', 'error');
                });
        });
    }

    // Xử lý nút thêm vào giỏ hàng
    const addToCartBtn = document.getElementById('addToCartBtn');
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', function () {
            const courseId = this.dataset.courseId;
            fetchWithAuth('/api/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ courseId: courseId })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showNotification('Đã thêm khóa học vào giỏ hàng!','success');
                        updateCartBadge();
                    } else {
                        showNotification(data.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
                    }
                })
                .catch(error => {
                    showNotification('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
                    console.error('Error:', error);
                });
        });
    }
}

/**
 * Kiểm tra trạng thái đăng ký khóa học
 * @param {string} courseId - ID khóa học
 * @param {HTMLElement} enrollBtn - Nút đăng ký học
 */
function checkEnrollmentStatus(courseId, enrollBtn) {
    const isAuthenticated = localStorage.getItem('isAuthenticated') === 'true';
    if (!isAuthenticated) return;

    // Gọi API kiểm tra đăng ký
    fetchWithAuth(`/api/enrollments/check/${courseId}`)
        .then(response => response.json())
        .then(data => {
            if (data.enrolled) {
                markAsEnrolled(enrollBtn);
            }
        })
        .catch(error => {
            console.error('Error checking enrollment status:', error);
        });
}

/**
 * Đánh dấu nút đã đăng ký và vô hiệu hóa
 * @param {HTMLElement} enrollBtn - Nút đăng ký học
 */
function markAsEnrolled(enrollBtn) {
    enrollBtn.textContent = 'Đã đăng ký';
    enrollBtn.disabled = true;
    enrollBtn.classList.remove('btn-primary');
    enrollBtn.classList.add('btn-success');

    // Ẩn nút thêm vào giỏ hàng nếu có
    const addToCartBtn = document.getElementById('addToCartBtn');
    if (addToCartBtn) {
        addToCartBtn.style.display = 'none';
    }

    // Hiển thị nút vào học
    const courseId = enrollBtn.dataset.courseId;

    // Tạo nút vào học nếu chưa có
    if (!document.getElementById('startLearningBtn')) {
        const startLearningBtn = document.createElement('a');
        startLearningBtn.id = 'startLearningBtn';
        startLearningBtn.href = `/learning/${courseId}`;
        startLearningBtn.className = 'btn btn-primary btn-lg w-100 mb-3';
        startLearningBtn.textContent = 'Vào học ngay';

        // Thêm vào sau nút đăng ký
        enrollBtn.parentNode.insertBefore(startLearningBtn, enrollBtn.nextSibling);
    }
}

/**
 * Đăng ký khóa học miễn phí
 * @param {string} courseId - ID khóa học
 * @param {HTMLElement} enrollBtn - Nút đăng ký học
 */
function enrollFreeCourse(courseId, enrollBtn) {
    fetchWithAuth('/api/enrollments/enroll', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({courseId: courseId})
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification('Đăng ký khóa học thành công!', 'success');
                markAsEnrolled(enrollBtn);
                // setTimeout(() => {
                //     window.location.href = '/learning/' + courseId;
                // }, 1500);
            } else {
                showNotification(data.message || 'Có lỗi xảy ra khi đăng ký khóa học', 'error');
            }
        })
        .catch(error => {
            showNotification('Có lỗi xảy ra khi đăng ký khóa học', 'error');
            console.error('Error:', error);
        });
}
/**
 * Khởi tạo tải thêm đánh giá
 */
function initializeReviewsLoader() {
    const loadMoreReviewsBtn = document.getElementById('loadMoreReviews');
    if (loadMoreReviewsBtn) {
        loadMoreReviewsBtn.addEventListener('click', function () {
            const currentPage = parseInt(this.getAttribute('data-current-page'));
            const nextPage = currentPage + 1;
            const totalPages = parseInt(this.getAttribute('data-total-pages'));
            const courseId = this.getAttribute('data-course-id');

            fetch(`/api/reviews/course/${courseId}?page=${nextPage}&size=5`)
                .then(response => response.json())
                .then(data => {
                    if (data && data.content) {
                        const reviewsList = document.querySelector('.reviews-list');
                        data.content.forEach(review => {
                            const reviewHtml = createReviewElement(review);
                            reviewsList.insertAdjacentHTML('beforeend', reviewHtml);
                        });

                        // Cập nhật trạng thái nút
                        this.setAttribute('data-current-page', nextPage);
                        if (nextPage >= totalPages - 1) {
                            this.style.display = 'none';
                        }
                    }
                })
                .catch(error => {
                    console.error('Error loading more reviews:', error);
                });
        });
    }
}

/**
 * Tạo HTML cho đánh giá
 */
function createReviewElement(review) {
    const stars = '<i class="fas fa-star"></i>'.repeat(review.rating) +
        '<i class="far fa-star"></i>'.repeat(5 - review.rating);

    return `
        <div class="review-item mb-4">
            <div class="review-header d-flex">
                <img src="/img/default-avatar.png" alt="User" class="review-avatar me-3">
                <div>
                    <h6 class="review-user-name">${review.userName}</h6>
                    <div class="review-meta d-flex align-items-center">
                        <div class="stars me-2">
                            ${stars}
                        </div>
                        <span class="review-date">${formatDate(review.createdAt)}</span>
                    </div>
                </div>
            </div>
            <div class="review-content mt-2">
                ${review.content}
            </div>
        </div>
    `;
}

/**
 * Định dạng ngày tháng
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

/**
 * Khởi tạo chức năng chia sẻ mạng xã hội
 */
function initializeSocialSharing() {
    // Chia sẻ Facebook
    document.querySelectorAll('.share-facebook').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const url = this.getAttribute('data-url');
            window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`, 'facebook-share', 'width=580,height=296');
        });
    });

    // Chia sẻ Twitter
    document.querySelectorAll('.share-twitter').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const url = this.getAttribute('data-url');
            const title = this.getAttribute('data-title');
            window.open(`https://twitter.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`, 'twitter-share', 'width=580,height=296');
        });
    });

    // Chia sẻ LinkedIn
    document.querySelectorAll('.share-linkedin').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const url = this.getAttribute('data-url');
            window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`, 'linkedin-share', 'width=580,height=296');
        });
    });

    // Chia sẻ Email
    document.querySelectorAll('.share-email').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            const url = this.getAttribute('data-url');
            const title = this.getAttribute('data-title');
            window.location.href = `mailto:?subject=${encodeURIComponent(title)}&body=${encodeURIComponent('Tôi nghĩ bạn sẽ thích khóa học này: ' + url)}`;
        });
    });
}
function updateBackButtonUrl() {
    // Tìm nút quay lại hiện có
    const backButton = document.querySelector('a.btn[href="/user/all-course"]');
    if (!backButton) return;

    // Xây dựng URL mới với thông tin phân trang
    const newUrl = buildBackUrl();

    // Cập nhật href của nút
    backButton.href = newUrl;
}

/**
 * Xây dựng URL để quay lại danh sách khóa học với tất cả tham số phân trang
 */
function buildBackUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const newParams = new URLSearchParams();

    // Ánh xạ tham số từ URL chi tiết sang URL danh sách
    const paramMapping = {
        'returnPage': 'page',
        'returnSize': 'size',
        'returnSort': 'sort',
        'returnSearch': 'search',
        'returnCategories': 'categories',
        'returnLevels': 'levels',
        'returnPriceTypes': 'priceTypes'
    };

    // Chuyển đổi các tham số
    for (const [returnParam, listParam] of Object.entries(paramMapping)) {
        const value = urlParams.get(returnParam);
        if (value) {
            newParams.set(listParam, value);
        }
    }

    // Đường dẫn cơ sở đến trang danh sách khóa học
    const baseUrl = '/user/all-course';

    return newParams.toString() ? `${baseUrl}?${newParams.toString()}` : baseUrl;
}
