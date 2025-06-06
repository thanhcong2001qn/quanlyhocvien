/**
 * Quản lý danh sách khóa học với URL Parameters
 * Tác giả: thanhcong2001qn
 * Cập nhật: 2025-05-13
 */

// Hàm khởi tạo khi trang được load
document.addEventListener('DOMContentLoaded', function() {
    // Tải danh sách danh mục
    loadCategories();

    // Khởi tạo giá trị từ URL hoặc giá trị mặc định
    initializeFromUrl();

    // Thiết lập các event listeners
    setupEventListeners();

    // Khởi tạo dropdown Bootstrap
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'))
    var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl)
    });
});

/**
 * Khởi tạo các giá trị từ URL hoặc sử dụng giá trị mặc định
 */
function initializeFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);

    // Cập nhật UI dựa trên URL params
    updateUIFromUrlParams(urlParams);

    // Tải khóa học dựa trên tham số URL
    loadCourses();
}

/**
 * Cập nhật UI dựa trên các tham số URL
 */
function updateUIFromUrlParams(urlParams) {
    // Cập nhật thanh tìm kiếm
    const search = urlParams.get('search') || '';
    document.getElementById('courseSearch').value = search;

    // Cập nhật dropdown sắp xếp
    const sort = urlParams.get('sort') || 'newest';
    document.querySelectorAll('.dropdown-item[data-sort]').forEach(item => {
        if (item.getAttribute('data-sort') === sort) {
            document.getElementById('sortDropdown').textContent = item.textContent;
        }
    });

    // Cập nhật các checkbox category
    const categories = (urlParams.get('categories') || '').split(',').filter(Boolean);
    setTimeout(() => {
        categories.forEach(categoryId => {
            const checkbox = document.getElementById(`category-${categoryId}`);
            if (checkbox) checkbox.checked = true;
        });
    }, 500); // Timeout để đảm bảo categories đã được render

    // Cập nhật các checkbox level
    const levels = (urlParams.get('levels') || '').split(',').filter(Boolean);
    levels.forEach(level => {
        const checkbox = document.getElementById(`level-${level}`);
        if (checkbox) checkbox.checked = true;
    });

    // Cập nhật các checkbox giá
    const priceTypes = (urlParams.get('priceTypes') || '').split(',').filter(Boolean);
    priceTypes.forEach(priceType => {
        const checkbox = document.getElementById(`price-${priceType}`);
        if (checkbox) checkbox.checked = true;
    });

    // Cập nhật các checkbox trạng thái đăng ký
    const enrollmentStatus = (urlParams.get('enrollmentStatus') || '').split(',').filter(Boolean);
    if (enrollmentStatus.length > 0) {
        document.querySelectorAll('.enrollment-filter').forEach(checkbox => {
            checkbox.checked = enrollmentStatus.includes(checkbox.value);
        });
    } else {
        // Mặc định chọn tất cả
        document.querySelectorAll('.enrollment-filter').forEach(checkbox => {
            checkbox.checked = true;
        });
    }
}

/**
 * Lấy các tham số từ URL hiện tại
 */
function getUrlParams() {
    return new URLSearchParams(window.location.search);
}

/**
 * Cập nhật URL với các tham số mới (thay đổi URL nhưng không reload trang)
 */
function updateUrlParams(params = {}) {
    const urlParams = getUrlParams();

    // Cập nhật từng tham số
    Object.entries(params).forEach(([key, value]) => {
        if (value === null || value === undefined || value === '') {
            urlParams.delete(key);
        } else {
            urlParams.set(key, value);
        }
    });

    // Cập nhật URL mà không reload trang
    const newUrl = `${window.location.pathname}?${urlParams.toString()}`;
    history.pushState({ path: newUrl }, '', newUrl);
}

// Thiết lập các sự kiện
function setupEventListeners() {
    // Xử lý tìm kiếm
    document.getElementById('searchButton').addEventListener('click', function() {
        const searchValue = document.getElementById('courseSearch').value.trim();
        updateUrlParams({
            search: searchValue,
            page: 0 // Reset về trang đầu tiên
        });
        loadCourses();
    });

    document.getElementById('courseSearch').addEventListener('keyup', function(event) {
        if (event.key === 'Enter') {
            const searchValue = this.value.trim();
            updateUrlParams({
                search: searchValue,
                page: 0 // Reset về trang đầu tiên
            });
            loadCourses();
        }
    });

    // Xử lý sắp xếp
    document.querySelectorAll('.dropdown-item[data-sort]').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const sortBy = this.getAttribute('data-sort');
            document.getElementById('sortDropdown').textContent = this.textContent;

            updateUrlParams({
                sort: sortBy,
                page: 0 // Reset về trang đầu tiên
            });
            loadCourses();
        });
    });

    // Xử lý áp dụng bộ lọc
    document.getElementById('applyFilters').addEventListener('click', function() {
        // Lấy danh mục đã chọn
        const categories = Array.from(document.querySelectorAll('.category-filter:checked'))
            .map(el => el.value)
            .join(',');

        // Lấy cấp độ đã chọn
        const levels = Array.from(document.querySelectorAll('.level-filter:checked'))
            .map(el => el.value)
            .join(',');

        // Lấy loại giá đã chọn
        const priceTypes = Array.from(document.querySelectorAll('.price-filter:checked'))
            .map(el => el.value)
            .join(',');

        // Lấy trạng thái đăng ký đã chọn
        const enrollmentStatus = Array.from(document.querySelectorAll('.enrollment-filter:checked'))
            .map(el => el.value)
            .join(',');

        updateUrlParams({
            categories: categories,
            levels: levels,
            priceTypes: priceTypes,
            enrollmentStatus: enrollmentStatus,
            page: 0 // Reset về trang đầu tiên
        });
        loadCourses();
    });


}

// Hàm tải danh mục từ API
async function loadCategories() {
    try {
        const response = await fetchWithAuth('/api/user/all-categories');
        if (!response.ok) {
            throw new Error('Không thể tải danh mục');
        }

        const categories = await response.json();
        renderCategories(categories);

        // Sau khi render categories, cập nhật lại UI từ URL params
        updateUIFromUrlParams(new URLSearchParams(window.location.search));
    } catch (error) {
        console.error('Lỗi khi tải danh mục:', error);
        document.getElementById('categoryFilters').innerHTML = '<p class="text-danger">Không thể tải danh mục</p>';
    }
}

// Hàm hiển thị danh mục
function renderCategories(categories) {
    const container = document.getElementById('categoryFilters');
    container.innerHTML = '';

    categories.forEach(category => {
        const checkboxDiv = document.createElement('div');
        checkboxDiv.className = 'form-check';

        const checkbox = document.createElement('input');
        checkbox.className = 'form-check-input category-filter';
        checkbox.type = 'checkbox';
        checkbox.value = category.categoryId;
        checkbox.id = `category-${category.categoryId}`;

        const label = document.createElement('label');
        label.className = 'form-check-label';
        label.htmlFor = `category-${category.categoryId}`;
        label.textContent = category.categoryName;

        checkboxDiv.appendChild(checkbox);
        checkboxDiv.appendChild(label);
        container.appendChild(checkboxDiv);
    });
}

// Hàm tải khóa học từ API
async function loadCourses() {
    try {
        // Hiển thị loading
        document.getElementById('loading').style.display = 'block';

        // Lấy tham số từ URL
        const urlParams = getUrlParams();
        const page = parseInt(urlParams.get('page')) || 0;
        const size = parseInt(urlParams.get('size')) || 6;
        const sort = urlParams.get('sort') || 'newest';
        const search = urlParams.get('search') || '';
        const categories = urlParams.get('categories') || '';
        const levels = urlParams.get('levels') || '';
        const priceTypes = urlParams.get('priceTypes') || '';
        const enrollmentStatus = urlParams.get('enrollmentStatus') || '';

        // Xây dựng URL với các tham số
        let url = `/api/user/all-courses?page=${page}&size=${size}`;

        // Thêm tham số sắp xếp
        if (sort) {
            url += `&sort=${sort}`;
        }

        // Thêm tham số tìm kiếm
        if (search) {
            url += `&search=${encodeURIComponent(search)}`;
        }

        // Thêm bộ lọc danh mục
        if (categories) {
            url += `&categories=${categories}`;
        }

        // Thêm bộ lọc cấp độ
        if (levels) {
            url += `&levels=${levels}`;
        }

        // Thêm bộ lọc giá
        if (priceTypes) {
            url += `&priceTypes=${priceTypes}`;
        }

        // Thêm bộ lọc trạng thái đăng ký
        if (enrollmentStatus && enrollmentStatus !== 'enrolled,not-enrolled') {
            url += `&enrollmentStatus=${enrollmentStatus}`;
        }

        // Gọi API với xác thực
        const response = await fetchWithAuth(url);
        if (!response.ok) {
            throw new Error('Không thể tải khóa học');
        }

        // Phân tích dữ liệu
        const data = await response.json();

        // Hiển thị khóa học và phân trang
        renderCourses(data.content);
        renderPagination(data.totalPages, page);
    } catch (error) {
        console.error('Lỗi khi tải khóa học:', error);
        document.getElementById('courseContainer').innerHTML =
            `<div class="col-12 alert alert-danger">
                <p>Không thể tải danh sách khóa học. Vui lòng thử lại sau.</p>
                <p>Lỗi: ${error.message}</p>
            </div>`;
    } finally {
        // Ẩn loading
        document.getElementById('loading').style.display = 'none';
    }
}

// Hàm hiển thị danh sách khóa học
function renderCourses(courses) {
    const container = document.getElementById('courseContainer');

    // Xóa nội dung cũ (ngoại trừ loading indicator)
    Array.from(container.children).forEach(child => {
        if (child.id !== 'loading') {
            child.remove();
        }
    });

    // Nếu không có khóa học nào
    if (!courses || courses.length === 0) {
        const noCoursesDiv = document.createElement('div');
        noCoursesDiv.className = 'col-12 text-center py-5';
        noCoursesDiv.innerHTML = '<p>Không tìm thấy khóa học nào phù hợp với tiêu chí của bạn.</p>';
        container.appendChild(noCoursesDiv);
        return;
    }

    // Hiển thị từng khóa học
    courses.forEach(course => {
        const courseCard = createCourseCard(course);
        container.appendChild(courseCard);
    });
}

// Hàm tạo thẻ khóa học
function createCourseCard(course) {
    const columnDiv = document.createElement('div');
    columnDiv.className = 'col-md-4 mb-4';

    // Kiểm tra xem khóa học có nổi bật hoặc mới không
    let badgeHtml = '';
    if (course.isFeatured) {
        badgeHtml = `<div class="badge-container">
                        <span class="badge bg-primary">Phổ biến</span>
                    </div>`;
    } else if (isNewCourse(course.publishedAt)) {
        badgeHtml = `<div class="badge-container">
                        <span class="badge bg-danger">Mới</span>
                    </div>`;
    }

    // Hiển thị badge đã đăng ký nếu có
    if (course.isEnrolled) {
        badgeHtml += `<div class="badge-container position-absolute" style="top: 10px; right: 10px;">
                        <span class="badge bg-success">Đã đăng ký</span>
                      </div>`;
    }

    // Định dạng giá
    let priceHtml = '';
    let priceClass = 'text-primary';
    if (course.price === 0 || course.price === null) {
        priceHtml = 'Miễn phí';
    } else {
        priceHtml = formatCurrency(course.price);
        priceClass = 'text-success';

        // Kiểm tra nếu có giá giảm
        if (course.discountPrice && course.discountPrice < course.price) {
            priceHtml = `<span class="text-decoration-line-through text-muted me-2">${formatCurrency(course.price)}</span>${formatCurrency(course.discountPrice)}`;
        }
    }

    // Tạo đường dẫn chi tiết khóa học với thông tin phân trang
    const urlParams = getUrlParams();
    let courseDetailUrl = `course-detail/${course.courseId}?returnPage=${urlParams.get('page') || '0'}`;

    // Thêm các tham số khác vào URL
    ['size', 'sort', 'search', 'categories', 'levels', 'priceTypes', 'enrollmentStatus'].forEach(param => {
        const value = urlParams.get(param);
        if (value) {
            courseDetailUrl += `&return${param.charAt(0).toUpperCase() + param.slice(1)}=${encodeURIComponent(value)}`;
        }
    });

    columnDiv.innerHTML = `
    <div class="card course-card position-relative" style="cursor: pointer;" onclick="window.location.href='${courseDetailUrl}'">
        ${badgeHtml}
        <div class="position-relative">
            <img src="${course.thumbnailPath || `https://via.placeholder.com/300x160?text=${encodeURIComponent(course.title)}`}" 
                class="card-img-top course-image" alt="${course.title}">
        </div>
        <div class="card-body d-flex flex-column">
            <h5 class="card-title">${course.title}</h5>
            <p class="card-text text-muted small">${course.description ? truncateText(course.description, 80) : ''}</p>
            <div class="mt-auto">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="badge bg-light text-dark">${course.level || 'Cơ bản'}</span>
                    <span class="${priceClass} fw-bold">${priceHtml}</span>
                </div>
                <div class="d-flex justify-content-between align-items-center">
                    <div class="small text-muted">
                        <i class="fas fa-users me-1"></i> ${formatNumber(course.totalStudents || 0)} học viên
                    </div>
                    <div class="small text-muted">
                        <i class="fas fa-star text-warning me-1"></i> ${course.rating || '0.0'}
                    </div>
                </div>
            </div>
        </div>
        <div class="card-overlay position-absolute" style="top: 0; left: 0; right: 0; bottom: 0; z-index: 1;"></div>
    </div>
`;

    return columnDiv;
}

// Hàm hiển thị phân trang
function renderPagination(totalPages, currentPage) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    if (totalPages <= 1) {
        return; // Không cần phân trang
    }

    // Nút "Trước"
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" ${currentPage === 0 ? 'tabindex="-1" aria-disabled="true"' : ''}>Trước</a>`;

    if (currentPage > 0) {
        prevLi.querySelector('a').addEventListener('click', function(e) {
            e.preventDefault();
            updateUrlParams({ page: currentPage - 1 });
            loadCourses();
        });
    }

    pagination.appendChild(prevLi);

    // Các trang số
    const maxPagesToShow = 5;
    const startPage = Math.max(0, Math.min(currentPage - Math.floor(maxPagesToShow / 2), totalPages - maxPagesToShow));
    const endPage = Math.min(startPage + maxPagesToShow - 1, totalPages - 1);

    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item ${i === currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;

        pageLi.querySelector('a').addEventListener('click', function(e) {
            e.preventDefault();
            updateUrlParams({ page: i });
            loadCourses();
        });

        pagination.appendChild(pageLi);
    }

    // Nút "Sau"
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" ${currentPage === totalPages - 1 ? 'tabindex="-1" aria-disabled="true"' : ''}>Sau</a>`;

    if (currentPage < totalPages - 1) {
        nextLi.querySelector('a').addEventListener('click', function(e) {
            e.preventDefault();
            updateUrlParams({ page: currentPage + 1 });
            loadCourses();
        });
    }

    pagination.appendChild(nextLi);
}

// Hàm kiểm tra khóa học mới (trong vòng 30 ngày)
function isNewCourse(publishedAt) {
    if (!publishedAt) return false;

    const publishDate = new Date(publishedAt);
    const now = new Date();
    const diffTime = Math.abs(now - publishDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return diffDays <= 30;
}

// Hàm định dạng tiền tệ (VND)
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

// Hàm định dạng số có dấu phân cách
function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}

// Hàm cắt ngắn văn bản nếu quá dài
function truncateText(text, maxLength) {
    if (!text) return '';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
}