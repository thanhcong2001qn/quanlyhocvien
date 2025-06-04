// Các biến toàn cục để theo dõi trạng thái
let currentPage = 0;
let pageSize = 6;
let totalPages = 0;
let currentSort = "newest"; // Mặc định sắp xếp theo ngày xuất bản mới nhất
let currentSearch = "";
let currentFilters = {
    categories: [],
    levels: [],
    priceTypes: [] // "free" hoặc "paid"
};

// Hàm khởi tạo khi trang được load
document.addEventListener('DOMContentLoaded', function() {
    // Tải danh sách danh mục
    loadCategories();

    // Tải các khóa học ban đầu
    loadCourses();

    // Thiết lập các event listeners
    setupEventListeners();
    var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'))
    var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl)
    });
    document.querySelectorAll('.dropdown-item[data-sort]').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const sortBy = this.getAttribute('data-sort');
            currentSort = sortBy;

            // Cập nhật nội dung của nút dropdown để hiển thị lựa chọn hiện tại
            const dropdownButton = document.getElementById('sortDropdown');
            dropdownButton.textContent = this.textContent;

            // Tải lại danh sách khóa học với sắp xếp mới
            currentPage = 0; // Reset về trang đầu tiên
            loadCourses();
        });
    });
});

// Thiết lập các sự kiện
function setupEventListeners() {
    // Xử lý tìm kiếm
    document.getElementById('searchButton').addEventListener('click', function() {
        currentSearch = document.getElementById('courseSearch').value.trim();
        currentPage = 0;
        loadCourses();
    });

    document.getElementById('courseSearch').addEventListener('keyup', function(event) {
        if (event.key === 'Enter') {
            currentSearch = document.getElementById('courseSearch').value.trim();
            currentPage = 0;
            loadCourses();
        }
    });

    // Xử lý sắp xếp
    document.querySelectorAll('.dropdown-item[data-sort]').forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            currentSort = this.dataset.sort;
            document.getElementById('sortDropdown').textContent = this.textContent;
            currentPage = 0;
            loadCourses();
        });
    });

    // Xử lý áp dụng bộ lọc
    document.getElementById('applyFilters').addEventListener('click', function() {
        // Lấy danh mục đã chọn
        currentFilters.categories = Array.from(document.querySelectorAll('.category-filter:checked')).map(el => el.value);

        // Lấy cấp độ đã chọn
        currentFilters.levels = Array.from(document.querySelectorAll('.level-filter:checked')).map(el => el.value);

        // Lấy loại giá đã chọn
        currentFilters.priceTypes = Array.from(document.querySelectorAll('.price-filter:checked')).map(el => el.value);

        currentPage = 0;
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

        // Xây dựng URL với các tham số
        let url = `/api/user/all-courses?page=${currentPage}&size=${pageSize}`;

        // Thêm tham số sắp xếp
        if (currentSort) {
            url += `&sort=${currentSort}`;
        }

        // Thêm tham số tìm kiếm
        if (currentSearch) {
            url += `&search=${encodeURIComponent(currentSearch)}`;
        }

        // Thêm bộ lọc danh mục
        if (currentFilters.categories && currentFilters.categories.length) {
            url += `&categories=${currentFilters.categories.join(',')}`;
        }

        // Thêm bộ lọc cấp độ
        if (currentFilters.levels && currentFilters.levels.length) {
            url += `&levels=${currentFilters.levels.join(',')}`;
        }

        // Thêm bộ lọc giá
        if (currentFilters.priceTypes && currentFilters.priceTypes.length) {
            url += `&priceTypes=${currentFilters.priceTypes.join(',')}`;
        }

        // Gọi API với xác thực
        const response = await fetchWithAuth(url);
        if (!response.ok) {
            throw new Error('Không thể tải khóa học');
        }

        // Phân tích dữ liệu
        const data = await response.json();

        // Cập nhật biến toàn cục
        totalPages = data.totalPages;

        // Hiển thị khóa học và phân trang
        renderCourses(data.content);
        renderPagination(data.totalPages);
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

    // Tạo đường dẫn chi tiết khóa học
    const courseDetailUrl = `course-detail/${course.courseId}`;

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
function renderPagination(totalPages) {
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
            currentPage--;
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
            currentPage = i;
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
            currentPage++;
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