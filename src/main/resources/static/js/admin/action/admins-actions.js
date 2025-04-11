// admin-actions.js
document.addEventListener('DOMContentLoaded', function () {
    attachAdminActionButtons();
});

function attachAdminActionButtons() {
    const tableBody = document.querySelector("#adminTable tbody");

    tableBody.addEventListener('click', function (e) {
        const editBtn = e.target.closest('.btn-edit');
        const deleteBtn = e.target.closest('.btn-delete');

        if (editBtn) {
            const adminId = editBtn.getAttribute('data-id');
            if (adminId) {
                window.location.href = '/adminDetail/' + adminId;
            }
        }

        if (deleteBtn) {
            const adminId = deleteBtn.getAttribute('data-id');
            showConfirmPopup('Bạn có chắc muốn xóa admin này?', function () {
                fetch('/admin/deleteAdmin/' + adminId, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error("Xóa thất bại");

                        const row = deleteBtn.closest('tr');
                        if (row) row.remove(); // ✅ Xóa dòng admin ra khỏi bảng ngay


                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá admin:', error);
                        alert('Đã xảy ra lỗi khi xoá admin.');
                    });
            });
        }
    });
}
