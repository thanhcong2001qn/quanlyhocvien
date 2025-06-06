// admin-actions.js
document.addEventListener('DOMContentLoaded', function () {
    attachAdminActionButtons();
});

function attachAdminActionButtons() {
    const tableBody = document.querySelector("#adminTable tbody");

    if (!tableBody) {
        console.warn("Không tìm thấy bảng #adminTable.");
        return;
    }

    tableBody.addEventListener('click', function (e) {
        const editBtn = e.target.closest('.btn-edit');
        const deleteBtn = e.target.closest('.btn-delete');

        // Xử lý nút Sửa
        if (editBtn) {
            const adminId = editBtn.getAttribute('data-id');
            if (adminId) {
                window.location.href = '/adminDetail/' + adminId;
            }
        }

        // Xử lý nút Xóa
        if (deleteBtn) {
            const adminId = deleteBtn.getAttribute('data-id');
            if (!adminId) return;

            showConfirmPopup('Bạn có chắc muốn xóa admin này?', function () {
                // Gọi API xóa
                fetch('/admin/deleteAdmin/' + adminId, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error("Xóa thất bại");

                        // ✅ Xoá khỏi bảng nếu thành công
                        const row = deleteBtn.closest('tr');
                        if (row) row.remove();

                        // ✅ Hiện thông báo
                        Swal.fire({
                            icon: 'success',
                            title: 'Xoá thành công',
                            text: 'Admin đã được xóa khỏi hệ thống.',
                            timer: 2000,
                            showConfirmButton: false
                        });

                        // (Tùy chọn) Tải lại bảng nếu cần:
                        // setTimeout(() => location.reload(), 2000);
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá admin:', error);
                        Swal.fire({
                            icon: 'error',
                            title: 'Lỗi',
                            text: 'Đã xảy ra lỗi khi xoá admin.',
                        });
                    });
            });
        }
    });
}
