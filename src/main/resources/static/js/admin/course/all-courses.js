// Khai báo biến toàn cục
let editCourseModal;
let allCourses = [];
let allCategories = [];
let currentPage = 0;
let itemsPerPage = 10;
let totalItems = 0;
let totalPages = 0;
let deleteId = null;
let filters = {
    search: '',
    categoryIds: '',
    levels: '',
    priceTypes: '',
    isPublished: '',
    sortBy: 'createdAt',
    sortDir: 'desc'
};

// Hàm tải danh mục
function fetchCategories() {
    fetchWithAuth('api/category/all-categories', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải danh sách danh mục');
            }
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                allCategories = data;
                populateCategoryFilter(data);
                // Cập nhật dropdown cho modal edit nếu đã hiển thị
                if (document.getElementById('editCourseModal').classList.contains('show')) {
                    populateEditCategoryDropdown();
                }
            } else {
                console.error('Dữ liệu danh mục không phải là mảng:', data);
            }
        })
        .catch(error => {
            console.error('Lỗi khi tải danh mục:', error);
        });
}

// Hàm cập nhật dropdown danh mục trong modal chỉnh sửa
function populateEditCategoryDropdown() {
    const categorySelect = document.getElementById('editCategoryId');

    // Xóa tất cả options hiện tại
    while (categorySelect.options.length > 1) {
        categorySelect.remove(1);
    }

    // Thêm các options mới từ allCategories
    if (Array.isArray(allCategories)) {
        allCategories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.categoryId;
            option.textContent = category.categoryName;
            categorySelect.appendChild(option);
        });
    }
}

// Hàm mở modal và tải dữ liệu khóa học
function openEditCourseModal(courseId) {
    // Hiển thị loading
    Swal.fire({
        title: 'Đang tải...',
        html: 'Vui lòng đợi trong giây lát',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    // Đảm bảo danh mục đã được tải
    if (!allCategories || !Array.isArray(allCategories) || allCategories.length === 0) {
        fetchCategories();
    }

    // Tải thông tin khóa học
    fetchWithAuth(`/api/courses/${courseId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải thông tin khóa học');
            }
            return response.json();
        })
        .then(course => {
            // Đóng loading
            Swal.close();

            // Cập nhật dropdown danh mục
            populateEditCategoryDropdown();

            // Điền thông tin vào form
            document.getElementById('editCourseId').value = course.courseId;
            document.getElementById('editTitle').value = course.title;
            document.getElementById('editDescription').value = course.description || '';
            document.getElementById('editPrice').value = course.price || 0;
            document.getElementById('editDiscountPrice').value = course.discountPrice || '';
            let categoryId = null;
            if (course.categoryId) {
                categoryId = course.categoryId;
            } else if (course.category && course.category.categoryId) {
                categoryId = course.category.categoryId;
            }

            // Thiết lập giá trị cho dropdown danh mục
            document.getElementById('editCategoryId').value = categoryId || '';
            document.getElementById('editLevel').value = course.level || '';
            document.getElementById('editDuration').value = course.duration || 0;
            document.getElementById('editIsPublished').checked = course.isPublished || false;
            document.getElementById('editIsFeatured').checked = course.isFeatured || false;

            // Hiển thị thumbnail hiện tại nếu có
            const currentThumbnailDiv = document.getElementById('currentThumbnail');
            if (course.thumbnailPath) {
                currentThumbnailDiv.innerHTML = `
                <div class="card mb-2">
                    <div class="card-header">Ảnh hiện tại</div>
                    <div class="card-body">
                        <img src="${course.thumbnailPath}" class="img-thumbnail" style="max-height: 150px" alt="Current thumbnail">
                    </div>
                </div>
            `;
            } else {
                currentThumbnailDiv.innerHTML = '<p class="text-muted">Chưa có ảnh thumbnail</p>';
            }

            // Reset preview thumbnail mới
            document.getElementById('editThumbnailPreview').innerHTML = '';
            document.getElementById('editThumbnail').value = '';

            // Hiển thị modal
            editCourseModal.show();
        })
        .catch(error => {
            console.error('Lỗi khi tải thông tin khóa học:', error);
            Swal.fire({
                icon: 'error',
                title: 'Lỗi!',
                text: error.message || 'Không thể tải thông tin khóa học'
            });
        });
}

// Hàm preview ảnh thumbnail khi chọn file
function previewEditThumbnail() {
    const previewContainer = document.getElementById('editThumbnailPreview');
    const file = document.getElementById('editThumbnail').files[0];

    if (file) {
        const reader = new FileReader();

        reader.onload = function(e) {
            previewContainer.innerHTML = `
                <div class="card mb-2">
                    <div class="card-header">Ảnh mới</div>
                    <div class="card-body">
                        <div class="position-relative">
                            <img src="${e.target.result}" class="img-thumbnail" style="max-height: 150px" alt="New thumbnail">
                            <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0" 
                                    id="removeEditThumbnail" title="Xóa ảnh">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `;

            // Thêm sự kiện xóa ảnh
            document.getElementById('removeEditThumbnail').addEventListener('click', function() {
                document.getElementById('editThumbnail').value = '';
                previewContainer.innerHTML = '';
            });
        };

        reader.readAsDataURL(file);
    } else {
        previewContainer.innerHTML = '';
    }
}

// Hàm kiểm tra form trước khi lưu
function validateEditForm() {
    let isValid = true;
    const title = document.getElementById('editTitle').value.trim();
    const categoryId = document.getElementById('editCategoryId').value;
    const price = document.getElementById('editPrice').value;
    const discountPrice = document.getElementById('editDiscountPrice').value;
    const thumbnailFile = document.getElementById('editThumbnail').files[0];

    // Reset thông báo lỗi
    document.querySelectorAll('#editCourseForm .invalid-feedback').forEach(el => {
        el.style.display = 'none';
    });
    document.querySelectorAll('#editCourseForm .is-invalid').forEach(el => {
        el.classList.remove('is-invalid');
    });

    // Kiểm tra tiêu đề
    if (!title) {
        document.getElementById('editTitle').classList.add('is-invalid');
        document.getElementById('editTitleError').style.display = 'block';
        isValid = false;
    }

    // Kiểm tra danh mục
    if (!categoryId) {
        document.getElementById('editCategoryId').classList.add('is-invalid');
        document.getElementById('editCategoryIdError').style.display = 'block';
        isValid = false;
    }

    // Kiểm tra giá và giá khuyến mãi
    if (price && discountPrice && parseFloat(discountPrice) >= parseFloat(price)) {
        document.getElementById('editDiscountPrice').classList.add('is-invalid');
        document.getElementById('editDiscountPriceError').style.display = 'block';
        isValid = false;
    }

    // Kiểm tra file thumbnail nếu có
    if (thumbnailFile) {
        // Kiểm tra định dạng file
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(thumbnailFile.type)) {
            document.getElementById('editThumbnail').classList.add('is-invalid');
            document.getElementById('editThumbnailError').style.display = 'block';
            document.getElementById('editThumbnailError').textContent = 'Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WEBP)';
            isValid = false;
        }

        // Kiểm tra kích thước file (tối đa 5MB)
        const maxSize = 5 * 1024 * 1024; // 5MB
        if (thumbnailFile.size > maxSize) {
            document.getElementById('editThumbnail').classList.add('is-invalid');
            document.getElementById('editThumbnailError').style.display = 'block';
            document.getElementById('editThumbnailError').textContent = 'Kích thước file không được vượt quá 5MB';
            isValid = false;
        }
    }

    return isValid;
}

// Hàm lưu thay đổi
function saveCourseChanges() {
    // Validate form
    if (!validateEditForm()) {
        return;
    }

    // Hiển thị loading
    const saveButton = document.getElementById('saveEditCourseBtn');
    const originalButtonText = saveButton.innerHTML;
    saveButton.disabled = true;
    saveButton.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Đang lưu...';

    // Tạo FormData từ form
    const form = document.getElementById('editCourseForm');
    const formData = new FormData(form);

    // Thêm các checkbox
    formData.set('isPublished', document.getElementById('editIsPublished').checked);
    formData.set('isFeatured', document.getElementById('editIsFeatured').checked);

    // Lấy courseId
    const courseId = document.getElementById('editCourseId').value;

    // Gửi request API để cập nhật khóa học
    fetchWithAuth(`/api/courses/update-course/${courseId}`, {
        method: 'PUT',
        body: formData,
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Có lỗi xảy ra khi cập nhật khóa học');
                }).catch(err => {
                    if (err instanceof SyntaxError) {
                        throw new Error(`Lỗi ${response.status}: ${response.statusText}`);
                    }
                    throw err;
                });
            }
            return response.json();
        })
        .then(data => {
            // Đóng modal
            editCourseModal.hide();

            // Hiển thị thông báo thành công
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: 'Đã cập nhật khóa học thành công',
                confirmButtonText: 'OK'
            }).then(() => {
                // Làm mới dữ liệu trang
                location.reload();
            });
        })
        .catch(error => {
            console.error('Lỗi khi cập nhật khóa học:', error);

            Swal.fire({
                icon: 'error',
                title: 'Lỗi!',
                text: error.message || 'Không thể cập nhật khóa học. Vui lòng thử lại sau.',
                confirmButtonText: 'OK'
            });
        })
        .finally(() => {
            // Khôi phục trạng thái ban đầu của nút
            saveButton.disabled = false;
            saveButton.innerHTML = originalButtonText;
        });
}

// Khởi tạo các biến và sự kiện khi DOM đã tải xong
document.addEventListener('DOMContentLoaded', function() {
    // Khởi tạo modal
    editCourseModal = new bootstrap.Modal(document.getElementById('editCourseModal'));

    // Thêm event listener cho nút lưu
    document.getElementById('saveEditCourseBtn').addEventListener('click', saveCourseChanges);

    // Thêm event listener cho file input
    document.getElementById('editThumbnail').addEventListener('change', previewEditThumbnail);

    // Định nghĩa hàm populateCategoryFilter trong scope này
    function populateCategoryFilter(categories) {
        const categoryFilter = document.getElementById('categoryFilter');
        categoryFilter.innerHTML = '<option value="">Tất cả danh mục</option>';

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.categoryId;
            option.textContent = category.categoryName;
            categoryFilter.appendChild(option);
        });
    }

    // Fetch khóa học từ API
    function fetchCourses() {
        // Hiển thị loading indicator
        const tableBody = document.getElementById('coursesTableBody');
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3"><i class="fas fa-spinner fa-spin me-2"></i>Đang tải dữ liệu...</td></tr>';

        // Xây dựng query params
        const params = new URLSearchParams();

        // Thêm các tham số lọc
        if (filters.search) params.append('search', filters.search);
        if (filters.categoryIds) params.append('categoryIds', filters.categoryIds);
        if (filters.levels) params.append('levels', filters.levels);
        if (filters.priceTypes) params.append('priceTypes', filters.priceTypes);
        if (filters.isPublished) params.append('isPublished', filters.isPublished);

        // Thêm tham số sắp xếp
        params.append('sortBy', filters.sortBy);
        params.append('sortDir', filters.sortDir);

        // Thêm tham số phân trang
        params.append('page', currentPage);
        params.append('size', itemsPerPage);

        // Gọi API
        fetchWithAuth(`/api/courses/all-courses?${params.toString()}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể tải danh sách khóa học');
                }
                return response.json();
            })
            .then(data => {
                allCourses = data.courses;
                totalItems = data.totalItems;
                totalPages = data.totalPages;
                updateStatistics(data);
                renderCourses(data.courses);
                setupPagination(data.totalItems, data.totalPages);
            })
            .catch(error => {
                console.error('Lỗi khi tải khóa học:', error);
                tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3 text-danger"><i class="fas fa-exclamation-circle me-2"></i>Lỗi khi tải dữ liệu</td></tr>';

                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi!',
                    text: 'Không thể tải danh sách khóa học. Vui lòng thử lại sau.',
                    confirmButtonText: 'OK'
                });
            });
    }

    // Cập nhật các thống kê
    function updateStatistics(data) {
        document.getElementById('totalCourses').textContent = data.totalItems || 0;

        // Giả sử API trả về thêm các thống kê này
        const published = data.publishedCount || 0;
        const draft = data.draftCount || 0;
        const featured = data.featuredCount || 0;

        document.getElementById('publishedCourses').textContent = published;
        document.getElementById('draftCourses').textContent = draft;
        document.getElementById('featuredCourses').textContent = featured;

        // Cập nhật thông tin hiển thị
        const start = currentPage * itemsPerPage + 1;
        const end = Math.min(start + data.courses.length - 1, data.totalItems);

        document.getElementById('displayingCount').textContent =
            `Hiển thị ${start} - ${end} trên ${data.totalItems} khóa học`;
    }

    // Render danh sách khóa học
    function renderCourses(courses) {
        const tableBody = document.getElementById('coursesTableBody');
        tableBody.innerHTML = '';

        if (courses.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="7" class="text-center py-3">Không tìm thấy khóa học nào</td>`;
            tableBody.appendChild(row);
            return;
        }

        // Hiển thị các khóa học
        courses.forEach(course => {
            const row = document.createElement('tr');

            // Tìm tên danh mục từ ID
            const category = allCategories.find(cat => cat.categoryId === course.category.categoryId);
            const categoryName = category ? category.categoryName : 'Chưa phân loại';

            // Đường dẫn mặc định cho thumbnail
            let thumbnailPath = course.thumbnailPath || '/images/default-course-thumbnail.png';

            // Hiển thị giá
            let priceDisplay = '';
            if (course.discountPrice && course.discountPrice < course.price) {
                priceDisplay = `
                    <span class="discount-price">${formatCurrency(course.discountPrice)}</span>
                    <br>
                    <span class="original-price">${formatCurrency(course.price)}</span>
                `;
            } else {
                priceDisplay = `<span class="price-tag">${formatCurrency(course.price)}</span>`;
            }

            // Hiển thị trạng thái
            let statusBadge = '';
            if (course.isPublished) {
                statusBadge = `<span class="badge bg-success">Đã xuất bản</span>`;
            } else {
                statusBadge = `<span class="badge bg-secondary">Chưa xuất bản</span>`;
            }

            // Thêm icon nổi bật nếu cần
            let featuredIcon = '';
            if (course.isFeatured) {
                featuredIcon = `<i class="fas fa-star featured-star ms-2" title="Khóa học nổi bật"></i>`;
            }

            row.innerHTML = `
                <td>
                    <img src="${thumbnailPath}" alt="${course.title}" class="course-thumbnail"
                        >
                </td>
                <td>
                    <div class="fw-medium">${course.title} ${featuredIcon}</div>
                    <small class="text-muted">${formatDuration(course.duration)}</small>
                </td>
                <td>
                    <span class="category-badge">${categoryName}</span>
                </td>
                <td>
                    <span class="badge ${getLevelBadgeClass(course.level)} badge-level">${course.level || 'Chưa xác định'}</span>
                </td>
                <td>${priceDisplay}</td>
                <td>${statusBadge}</td>
                <td class="action-buttons">
                    <a href="/view-course/${course.courseId}" class="btn btn-sm btn-outline-info me-1" title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-primary me-1" title="Chỉnh sửa" 
                            onclick="openEditCourseModal(${course.courseId})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-btn"
                            data-id="${course.courseId}"
                            data-title="${course.title}"
                            title="Xóa">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            `;

            tableBody.appendChild(row);
        });

        // Thêm event listeners cho các nút xóa
        document.querySelectorAll('.delete-btn').forEach(button => {
            button.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                const title = this.getAttribute('data-title');
                openDeleteModal(id, title);
            });
        });
    }

    // Thiết lập phân trang
    function setupPagination(totalItems, totalPages) {
        const pagination = document.getElementById('pagination');
        pagination.innerHTML = '';

        // Không hiển thị phân trang nếu chỉ có 1 trang
        if (totalPages <= 1) {
            return;
        }

        // Nút trang trước
        const prevLi = document.createElement('li');
        prevLi.classList.add('page-item');
        if (currentPage === 0) prevLi.classList.add('disabled');
        prevLi.innerHTML = `<a class="page-link" href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a>`;
        prevLi.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentPage > 0) {
                currentPage--;
                fetchCourses();
            }
        });
        pagination.appendChild(prevLi);

        // Các nút số trang
        const maxVisiblePages = 5;
        const startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        const endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

        for (let i = startPage; i <= endPage; i++) {
            const pageLi = document.createElement('li');
            pageLi.classList.add('page-item');
            if (i === currentPage) pageLi.classList.add('active');
            // Hiển thị số trang bắt đầu từ 1 cho người dùng, nhưng API bắt đầu từ 0
            pageLi.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
            pageLi.addEventListener('click', function(e) {
                e.preventDefault();
                currentPage = i;
                fetchCourses();
            });
            pagination.appendChild(pageLi);
        }

        // Nút trang sau
        const nextLi = document.createElement('li');
        nextLi.classList.add('page-item');
        if (currentPage === totalPages - 1) nextLi.classList.add('disabled');
        nextLi.innerHTML = `<a class="page-link" href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a>`;
        nextLi.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentPage < totalPages - 1) {
                currentPage++;
                fetchCourses();
            }
        });
        pagination.appendChild(nextLi);
    }

    // Định dạng thời lượng khóa học
    function formatDuration(minutes) {
        if (!minutes) return 'Chưa xác định';

        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;

        if (hours > 0) {
            return `${hours} giờ ${remainingMinutes > 0 ? remainingMinutes + ' phút' : ''}`;
        } else {
            return `${minutes} phút`;
        }
    }

    // Định dạng tiền tệ
    function formatCurrency(amount) {
        if (amount === 0) return 'Miễn phí';

        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    // Lấy class cho badge cấp độ
    function getLevelBadgeClass(level) {
        if (!level) return 'bg-secondary';

        switch(level) {
            case 'Cơ bản':
            case 'BEGINNER':
                return 'bg-info';
            case 'Trung cấp':
            case 'INTERMEDIATE':
                return 'bg-warning';
            case 'Nâng cao':
            case 'ADVANCED':
                return 'bg-danger';
            case 'Tất cả cấp độ':
            case 'ALL_LEVELS':
                return 'bg-primary';
            default:
                return 'bg-secondary';
        }
    }

    // Mở modal xác nhận xóa
    function openDeleteModal(id, title) {
        document.getElementById('courseToDelete').textContent = title;
        deleteId = id;
        const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
        deleteModal.show();
    }

    // Xóa khóa học
    function deleteCourse(id) {
        fetchWithAuth(`/api/courses/delete-course/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể xóa khóa học');
                }
                return response.json();
            })
            .then(data => {
                Swal.fire({
                    icon: 'success',
                    title: 'Thành công!',
                    text: 'Đã xóa khóa học thành công',
                    confirmButtonText: 'OK'
                });

                // Cập nhật lại danh sách sau khi xóa
                fetchCourses();
            })
            .catch(error => {
                console.error('Lỗi khi xóa khóa học:', error);
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi!',
                    text: 'Không thể xóa khóa học. Vui lòng thử lại sau.',
                    confirmButtonText: 'OK'
                });
            });
    }

    // Thêm xử lý lọc theo giá
    function setupPriceTypeFilter() {
        const priceTypeFilter = document.getElementById('priceTypeFilter');
        if (priceTypeFilter) {
            priceTypeFilter.innerHTML = `
                <option value="">Tất cả loại giá</option>
                <option value="free">Miễn phí</option>
                <option value="paid">Có phí</option>
                <option value="discounted">Giảm giá</option>
            `;
        }
    }

    // Thiết lập các tùy chọn sắp xếp
    function setupSortOptions() {
        const sortOptions = document.getElementById('sortOptions');
        if (sortOptions) {
            sortOptions.innerHTML = `
                <option value="createdAt-desc">Mới nhất</option>
                <option value="createdAt-asc">Cũ nhất</option>
                <option value="title-asc">Tên A-Z</option>
                <option value="title-desc">Tên Z-A</option>
                <option value="price-asc">Giá thấp đến cao</option>
                <option value="price-desc">Giá cao đến thấp</option>
            `;
        }
    }

    // Thiết lập các tùy chọn kích thước trang
    function setupPageSizeOptions() {
        const pageSizeSelect = document.getElementById('pageSizeSelect');
        if (pageSizeSelect) {
            pageSizeSelect.innerHTML = `
                <option value="10">10 dòng</option>
                <option value="25">25 dòng</option>
                <option value="50">50 dòng</option>
                <option value="100">100 dòng</option>
            `;
        }
    }

    // Xử lý sự kiện nút tìm kiếm
    document.getElementById('searchButton').addEventListener('click', function() {
        // Lấy giá trị các bộ lọc
        filters.categoryIds = document.getElementById('categoryFilter').value;
        filters.isPublished = document.getElementById('statusFilter').value;
        filters.levels = document.getElementById('levelFilter').value;
        filters.search = document.getElementById('searchInput').value.trim();

        // Lấy giá trị lọc theo giá nếu có
        const priceTypeFilter = document.getElementById('priceTypeFilter');
        if (priceTypeFilter) {
            filters.priceTypes = priceTypeFilter.value;
        }

        // Reset về trang đầu tiên khi thay đổi bộ lọc
        currentPage = 0;

        // Tải lại danh sách khóa học
        fetchCourses();
    });

    // Xử lý sự kiện nhấn Enter trong input tìm kiếm
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('searchButton').click();
        }
    });

    // Xử lý sự kiện nút xác nhận xóa
    document.getElementById('confirmDelete').addEventListener('click', function() {
        const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
        modal.hide();
        if (deleteId) {
            deleteCourse(deleteId);
        }
    });

    // Xử lý sự kiện sắp xếp
    const sortOptions = document.getElementById('sortOptions');
    if (sortOptions) {
        sortOptions.addEventListener('change', function() {
            const selectedOption = this.value;

            // Phân tích tùy chọn sắp xếp, định dạng: field-direction
            const [field, direction] = selectedOption.split('-');

            if (field && direction) {
                filters.sortBy = field;
                filters.sortDir = direction;
                currentPage = 0; // Reset về trang đầu tiên khi thay đổi sắp xếp
                fetchCourses();
            }
        });
    }

    // Xử lý sự kiện thay đổi số lượng item trên mỗi trang
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            itemsPerPage = parseInt(this.value);
            currentPage = 0; // Reset về trang đầu tiên khi thay đổi kích thước trang
            fetchCourses();
        });
    }

    // Khởi chạy khi trang tải xong
    setupPriceTypeFilter();
    setupSortOptions();
    setupPageSizeOptions();
    fetchCategories();
    fetchCourses();
});