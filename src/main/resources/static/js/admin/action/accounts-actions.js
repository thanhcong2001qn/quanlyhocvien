function setupAccountActionButtons() {
    // 🟩 Sửa tài khoản
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', () => {
            const accountId = button.getAttribute('data-id');
            if (accountId) {
                window.location.href = `/accountDetail/${accountId}`;
            }
        });
    });

    // 🟥 Vô hiệu hóa tài khoản (hiện tại vẫn dùng nút, có thể xoá phần này nếu chỉ dùng toggle)
    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', () => {
            const accountId = button.getAttribute('data-id');
            if (!accountId) return;

            Swal.fire({
                title: 'Xác nhận vô hiệu hóa',
                text: 'Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: 'Vô hiệu hóa',
                cancelButtonText: 'Hủy'
            }).then((result) => {
                if (result.isConfirmed) {
                    fetch(`/account/deactivate/${accountId}`, {
                        method: 'PUT'
                    })
                    .then(response => {
                        if (!response.ok) throw new Error('Vô hiệu hóa thất bại');
                        return response.text();
                    })
                    .then(() => {
                        Swal.fire('Thành công!', 'Tài khoản đã bị vô hiệu hóa.', 'success')
                            .then(() => fetchAccounts()); // chỉ reload bảng
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi vô hiệu hóa:', error);
                        Swal.fire('Lỗi', 'Không thể vô hiệu hóa tài khoản.', 'error');
                    });
                }
            });
        });
    });

    // 🔄 Toggle trạng thái (mới)
   document.querySelectorAll('.toggle-status').forEach(toggle => {
       toggle.addEventListener('change', () => {
           const accountId = toggle.getAttribute('data-id');
           const isActive = toggle.checked;

           const action = isActive ? 'mở khóa' : 'vô hiệu hóa';
           const url = `/account/updateStatus/${accountId}`;

           fetch(url, {
               method: 'PUT',
               headers: { 'Content-Type': 'application/json' },
               body: JSON.stringify({ isActive })
           })
           .then(response => {
               if (!response.ok) throw new Error(`${action} thất bại`);
               return response.text();
           })
           .then(() => {
               // ✅ Cập nhật trực tiếp giao diện
               const row = toggle.closest('tr');
               const statusBadge = row.querySelector('.status-badge');
               statusBadge.textContent = isActive ? 'Hoạt động' : 'Đã khóa';
               statusBadge.className = `status-badge ${isActive ? 'status-active' : 'status-inactive'}`;

               Swal.fire('Thành công!', `Tài khoản đã được ${action}.`, 'success');
           })
           .catch(error => {
               console.error(`❌ Lỗi khi ${action}:`, error);
               Swal.fire('Lỗi', `Không thể ${action} tài khoản.`, 'error');
               toggle.checked = !isActive; // revert lại nếu lỗi
           });
       });
   });
}

// ✅ Gọi sau khi DOM load
document.addEventListener('DOMContentLoaded', () => {
    setupAccountActionButtons();
});
