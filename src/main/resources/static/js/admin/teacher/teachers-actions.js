document.addEventListener('DOMContentLoaded', function () {

  // === XỬ LÝ NÚT SỬA ===
  document.querySelectorAll('.btn-edit').forEach(button => {
    button.addEventListener('click', function () {
      const teacherId = this.getAttribute('data-id');
      window.location.href = `/teacherDetail/${teacherId}`; // redirect tới trang form
    });
  });

  // === XỬ LÝ NÚT XÓA ===
 document.querySelectorAll('.btn-delete').forEach(button => {
   button.addEventListener('click', function () {
     const teacherId = this.getAttribute('data-id');
     const row = this.closest('tr');

     showConfirmPopup("Bạn có chắc chắn muốn xóa giáo viên này?", () => {
       fetch(`/teacher/deleteTeacher/${teacherId}`, {
         method: 'DELETE'
       })
       .then(response => {
         if (!response.ok) throw new Error("Xóa thất bại");

         if (row) row.remove();

         const tbody = document.querySelector('table tbody');
         if (tbody && tbody.children.length === 0) {
           const emptyRow = document.createElement('tr');
           emptyRow.innerHTML = `<td colspan="6" class="text-center">Không tìm thấy giáo viên nào</td>`;
           tbody.appendChild(emptyRow);
         }
       })
       .catch(error => {
         console.error(error);
         alert("Đã xảy ra lỗi khi xóa giáo viên.");
       });
     });
   });
 });
});
