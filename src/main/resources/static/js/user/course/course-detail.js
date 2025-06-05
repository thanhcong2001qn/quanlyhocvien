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
    initializeCourseActions();
    initializeReviewsLoader();
    initializeSocialSharing();
    initializePreviewVideo();
    initializeCourseAccess();
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
let youtubePlayer;

function showVideoPreviewModal(lesson) {
    // Xóa modal cũ
    const existingModal = document.getElementById('videoPreviewModal');
    if (existingModal) {
        existingModal.remove();
    }

    const videoId = extractYouTubeId(lesson.videoUrl);

    if (!videoId) {
        alert('URL video YouTube không hợp lệ');
        return;
    }

    // Tạo modal với div cho YouTube player
    const modalHTML = `
        <div class="modal fade" id="videoPreviewModal" tabindex="-1" aria-labelledby="videoPreviewTitle" aria-hidden="false">
            <div class="modal-dialog modal-lg modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="videoPreviewTitle">Xem trước: ${lesson.title}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body p-0">
                        <div class="ratio ratio-16x9">
                            <div id="youtubePlayerContainer"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // Khởi tạo YouTube player
    youtubePlayer = new YT.Player('youtubePlayerContainer', {
        videoId: videoId,
        playerVars: {
            'autoplay': 1,
            'rel': 0,
            'modestbranding': 1
        },
        events: {
            'onReady': onPlayerReady
        }
    });

    function onPlayerReady(event) {
        event.target.playVideo();
    }

    // Hiển thị modal
    const videoModal = document.getElementById('videoPreviewModal');
    const bsModal = new bootstrap.Modal(videoModal);
    bsModal.show();

    // Xử lý khi đóng modal
    videoModal.addEventListener('hidden.bs.modal', function () {
        if (youtubePlayer) {
            youtubePlayer.stopVideo();
            youtubePlayer.destroy();
        }
        setTimeout(() => videoModal.remove(), 300);
    });
}
function extractYouTubeId(url) {
    if (!url) return null;

    // Mẫu phổ biến: youtube.com/watch?v=VIDEO_ID
    // hoặc youtu.be/VIDEO_ID
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|&v=)([^#&?]*).*/;
    const match = url.match(regExp);

    return (match && match[2].length === 11) ? match[2] : null;
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

        // Kiểm tra trạng thái đăng ký khóa học ngay khi trang load
        checkEnrollmentStatus(courseId, enrollBtn);

        enrollBtn.addEventListener('click', function () {

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
function checkEnrollmentStatusAsync(courseId) {

    return fetchWithAuth(`/api/enrollments/check/${courseId}`)
        .then(response => response.json())
        .then(data => {
            return data.enrolled === true;
        })
        .catch(error => {
            console.error('Error checking enrollment status:', error);
            return false;
        });
}
function setupLessonVideoAccess(isEnrolled) {
    document.querySelectorAll('.lecture-item').forEach(item => {
        const previewBadge = item.querySelector('.lecture-preview');
        const lessonTitle = item.querySelector('.lecture-title').textContent;
        const lessonId = item.dataset.lessonId;
        let videoUrl = '';

        try {
            videoUrl = item.querySelector('[data-video-url]')?.dataset.videoUrl || '';
        } catch (e) {
            console.warn('Could not find video URL for lesson:', lessonTitle);
        }

        const lessonData = {
            id: lessonId,
            title: lessonTitle,
            videoUrl: videoUrl
        };

        // Xóa event listener cũ nếu có
        const newItem = item.cloneNode(true);
        item.parentNode.replaceChild(newItem, item);

        // Nếu là bài học miễn phí hoặc học viên đã đăng ký khóa học
        if (previewBadge || isEnrolled) {
            newItem.addEventListener('click', function() {
                showVideoPreviewModal(lessonData);
            });
            newItem.style.cursor = 'pointer';

            // Thêm biểu tượng play để chỉ ra có thể xem video
            if (!newItem.querySelector('.can-play-indicator')) {
                const indicator = document.createElement('span');
                indicator.className = 'can-play-indicator ms-2';
                indicator.innerHTML = '<i class="fas fa-play-circle text-primary"></i>';
                newItem.querySelector('.lecture-title').appendChild(indicator);
            }
        } else {
            // Bài học không miễn phí và chưa đăng ký
            newItem.addEventListener('click', function() {
                showEnrollmentPrompt();
            });
            newItem.style.cursor = 'not-allowed';

            // Thêm biểu tượng khóa
            if (!newItem.querySelector('.locked-indicator')) {
                const indicator = document.createElement('span');
                indicator.className = 'locked-indicator ms-2';
                indicator.innerHTML = '<i class="fas fa-lock text-secondary"></i>';
                newItem.querySelector('.lecture-title').appendChild(indicator);
            }
        }
    });
}

function showEnrollmentPrompt() {
    const courseTitle = document.querySelector('h1.course-title').textContent;
    const coursePrice = document.getElementById('enrollBtn')?.dataset.price || '';
    const priceDisplay = coursePrice ? `${coursePrice}đ` : 'Miễn phí';

    const modalHTML = `
        <div class="modal fade" id="enrollmentPromptModal" tabindex="-1" aria-hidden="false">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Đăng ký khóa học</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="text-center mb-4">
                            <i class="fas fa-lock fa-3x text-primary mb-3"></i>
                            <h5>Nội dung này chỉ dành cho học viên đã đăng ký</h5>
                            <p>Đăng ký khóa học "${courseTitle}" với giá ${priceDisplay} để xem toàn bộ nội dung.</p>
                        </div>
                    </div>
                    <div class="modal-footer justify-content-center">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                        <button type="button" class="btn btn-primary" id="promptEnrollBtn">Đăng ký ngay</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // Xóa modal cũ nếu tồn tại
    const existingModal = document.getElementById('enrollmentPromptModal');
    if (existingModal) {
        existingModal.remove();
    }

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // Hiển thị modal
    const promptModal = document.getElementById('enrollmentPromptModal');
    const bsModal = new bootstrap.Modal(promptModal);
    bsModal.show();

    // Xử lý nút đăng ký trong modal
    document.getElementById('promptEnrollBtn').addEventListener('click', function() {
        bsModal.hide();
        // Kích hoạt nút đăng ký chính
        document.getElementById('enrollBtn').click();
    });
}

/**
 * Đánh dấu nút đã đăng ký và vô hiệu hóa
 * @param {HTMLElement} enrollBtn - Nút đăng ký học
 */
function markAsEnrolled(enrollBtn) {
    // Kiểm tra xem enrollBtn có tồn tại không
    if (!enrollBtn) {
        console.log("Nút đăng ký không tồn tại, tạo mới...");

        // Tìm container phù hợp dựa trên cấu trúc HTML đã cung cấp
        const actionContainer = document.querySelector('.card.course-purchase-card .card-body div:not(.course-price):not(.course-guarantee):not(.course-includes):not(.share-course)');

        if (!actionContainer) {
            console.warn('Không tìm thấy container để thêm nút đăng ký');
            return;
        }

        // Lấy courseId từ nút "Thêm vào giỏ hàng" nếu có
        const addToCartBtn = document.getElementById('addToCartBtn');
        const courseId = addToCartBtn?.dataset.courseId;

        if (!courseId) {
            console.warn('Không thể xác định ID khóa học');
            return;
        }

        // Xóa nội dung hiện tại của container
        actionContainer.innerHTML = '';

        // Tạo nút "Đã đăng ký"
        enrollBtn = document.createElement('button');
        enrollBtn.id = 'enrollBtn';
        enrollBtn.className = 'btn btn-success btn-lg w-100 mb-3';
        enrollBtn.textContent = 'Đã đăng ký';
        enrollBtn.disabled = true;
        enrollBtn.dataset.courseId = courseId;

        // Thêm biểu tượng check
        const checkIcon = document.createElement('i');
        checkIcon.className = 'fas fa-check-circle me-2';
        enrollBtn.prepend(checkIcon);

        // Thêm nút vào container
        actionContainer.appendChild(enrollBtn);

        console.log('Đã tạo nút Đã đăng ký');
    } else {
        // Nếu nút đã tồn tại, cập nhật trạng thái
        console.log("Cập nhật nút đăng ký thành Đã đăng ký");
        enrollBtn.textContent = 'Đã đăng ký';
        enrollBtn.disabled = true;
        enrollBtn.classList.remove('btn-primary', 'btn-outline-primary');
        enrollBtn.classList.add('btn-success');

        // Thêm biểu tượng check nếu chưa có
        if (!enrollBtn.querySelector('.fa-check-circle')) {
            const checkIcon = document.createElement('i');
            checkIcon.className = 'fas fa-check-circle me-2';
            enrollBtn.prepend(checkIcon);
        }
    }

    // Ẩn nút thêm vào giỏ hàng nếu có
    const addToCartBtn = document.getElementById('addToCartBtn');
    if (addToCartBtn) {
        // Xóa nút thêm vào giỏ hàng khỏi DOM thay vì chỉ ẩn đi
        addToCartBtn.parentElement?.removeChild(addToCartBtn);
    }

    // Cập nhật phần div để hiển thị trạng thái đã đăng ký
    const enrollContainer = enrollBtn.closest('div[th\\:if]');
    if (enrollContainer) {
        // Xóa điều kiện th:if vì chúng ta đã xử lý ở phía client
        enrollContainer.removeAttribute('th:if');
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
function initializeCourseAccess() {
    let enrollBtn = document.getElementById('enrollBtn')
    const courseId = document.getElementById('enrollBtn')?.dataset.courseId ||
        document.getElementById('addToCartBtn')?.dataset.courseId;
    if (!courseId) {
        console.warn('Course ID not found on page');
        return;
    }

    // Đã đăng nhập, kiểm tra trạng thái đăng ký
    checkEnrollmentStatusAsync(courseId)
        .then(isEnrolled => {
            console.log(`Trạng thái đăng ký khóa học: ${isEnrolled ? 'Đã đăng ký' : 'Chưa đăng ký'}`);
            if(isEnrolled){
                markAsEnrolled(enrollBtn);
            }
            // Thiết lập truy cập bài học dựa trên trạng thái đăng ký
            setupLessonVideoAccess(isEnrolled);
        })
        .catch(error => {
            console.error('Lỗi khi kiểm tra trạng thái đăng ký:', error);
            // Mặc định chỉ hiện bài học miễn phí nếu có lỗi
            setupLessonVideoAccess(false);
        });
}