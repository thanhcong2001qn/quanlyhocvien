document.addEventListener('DOMContentLoaded', function() {
    fetchCategories();
    // Khởi tạo CKEditor cho mô tả
    ClassicEditor
        .create(document.querySelector('#description'))
        .catch(error => {
            console.error(error);
        });

    // Hiển thị preview ảnh thumbnail khi chọn file
    document.getElementById('thumbnail').addEventListener('change', function() {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const preview = document.getElementById('thumbnailPreview');
                preview.src = e.target.result;
                preview.style.display = 'block';
            }
            reader.readAsDataURL(file);
        }
    });

    // Xử lý submit form
    document.getElementById('addCourseForm').addEventListener('submit', function(e) {
        e.preventDefault();

        // Validate form
        if (!validateForm()) {
            return;
        }

        // Tạo FormData để gửi dữ liệu form bao gồm cả file
        const formData = new FormData(this);

        // Gửi request API để tạo khóa học mới
        fetchWithAuth('/api/courses/add-course', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Có lỗi xảy ra khi tạo khóa học');
                }
                return response.json();
            })
            .then(data => {
                Swal.fire({
                    icon: 'success',
                    title: 'Thành công!',
                    text: 'Đã tạo khóa học mới thành công',
                    confirmButtonText: 'OK'
                }).then((result) => {
                    if (result.isConfirmed) {
                        window.location.href = '/all-courses';
                    }
                });
            })
            .catch(error => {
                Swal.fire({
                    icon: 'error',
                    title: 'Lỗi!',
                    text: error.message,
                    confirmButtonText: 'OK'
                });
            });
    });

    // Hàm validate form
    function validateForm() {
        const title = document.getElementById('title').value.trim();
        const category = document.getElementById('category').value;

        if (!title) {
            Swal.fire({
                icon: 'warning',
                title: 'Thiếu thông tin!',
                text: 'Vui lòng nhập tên khóa học',
                confirmButtonText: 'OK'
            });
            return false;
        }

        if (!category) {
            Swal.fire({
                icon: 'warning',
                title: 'Thiếu thông tin!',
                text: 'Vui lòng chọn danh mục cho khóa học',
                confirmButtonText: 'OK'
            });
            return false;
        }

        return true;
    }
});
function fetchCategories() {
    // Hiển thị loading state (nếu cần)
    const categorySelect = document.getElementById('category');
    categorySelect.innerHTML = '<option value="" selected disabled>Đang tải danh mục...</option>';

    // Gọi API để lấy danh sách danh mục
    fetchWithAuth('api/user/all-categories', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            // Thêm các headers cần thiết như Authorization nếu cần
        },
        credentials: 'include' // Đảm bảo cookies được gửi cùng request để xác thực
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể lấy danh sách danh mục');
            }
            return response.json();
        })
        .then(data => {
            // Xóa option loading
            categorySelect.innerHTML = '<option value="" selected disabled>Chọn danh mục</option>';

            // Kiểm tra nếu data là mảng
            if (Array.isArray(data)) {
                // Tạo các options cho dropdown từ dữ liệu API
                data.forEach(category => {
                    const option = document.createElement('option');
                    option.value = category.categoryId;
                    option.textContent = category.categoryName;
                    categorySelect.appendChild(option);
                });
            } else {
                console.error('Dữ liệu trả về không phải là mảng:', data);
            }
        })
        .catch(error => {
            console.error('Lỗi khi lấy danh mục:', error);
            categorySelect.innerHTML = '<option value="" selected disabled>Không thể tải danh mục</option>';

            // Hiển thị thông báo lỗi cho người dùng
            Swal.fire({
                icon: 'error',
                title: 'Lỗi!',
                text: 'Không thể tải danh sách danh mục khóa học. Vui lòng thử lại sau.',
                confirmButtonText: 'OK'
            });
        });
}