// public/js/admin/list/students-list.js

let currentStudentPage = 0;
const studentPageSize = 5;
let hasFocusedStudentPageInput = false;
const debouncedHandlePageInput = debounce(handlePageInput, 400);

async function fetchStudents(page = 0) {
    try {
        showLoading();
        const response = await fetch(`/student/api/students?page=${page}&size=${studentPageSize}`);
        if (!response.ok) {
            throw new Error('Lỗi khi fetch students');
        }
        const data = await response.json();
        renderStudentTable(data.content);
        renderStudentPagination(data.totalPages, page);
        attachStudentActionButtons();
    } catch (error) {
        console.error('❌ Lỗi fetch student:', error);
    } finally {
        hideLoading();
    }
}

function renderStudentTable(students) {
    const tbody = document.querySelector(".table tbody");
    tbody.innerHTML = "";

    if (students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center">Không tìm thấy sinh viên nào</td></tr>`;
        return;
    }

    students.forEach(student => {
        const row = `<tr>
            <td>${student.studentId}</td>
            <td>${student.fullName || 'N/A'}</td>
            <td>
                <span class="gender-badge ${student.gender === 'Nam' ? 'male' : 'female'}">
                    ${student.gender || 'N/A'}
                </span>
            </td>
            <td>${student.className || 'N/A'}</td>
            <td>${student.dateOfBirth ? formatDate(student.dateOfBirth) : 'N/A'}</td>
            <td>${student.address || 'N/A'}</td>
            <td>${student.phoneNumber || 'N/A'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-icon btn-edit" data-id="${student.studentId}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-icon btn-delete" data-id="${student.studentId}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function renderStudentPagination(totalPages, currentPage) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = `
        <div class="pagination-wrapper d-flex align-items-center justify-content-center gap-3">
            <button class="btn btn-pagination" onclick="prevStudentPage()" ${currentPage === 0 ? 'disabled' : ''}>
                <i class="fas fa-arrow-left"></i>
            </button>

            <div class="page-info d-flex align-items-center gap-2">
                <span>Trang</span>
                <input
                    type="number"
                    id="pageInput"
                    min="1"
                    max="${totalPages}"
                    value="${currentPage + 1}"
                    oninput="debouncedHandlePageInput(this.value, ${totalPages})"
                >
                <span class="page-total">/ ${totalPages}</span>
            </div>

            <button class="btn btn-pagination" onclick="nextStudentPage(${totalPages})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;

    setTimeout(() => {
        const pageInput = document.getElementById('pageInput');
                if (pageInput) {
                    if (hasFocusedStudentPageInput) {
                        // ✅ Chỉ focus nếu người dùng đã từng focus trước đó
                        pageInput.focus();
                        pageInput.select();
                    }

                    pageInput.addEventListener('focus', function () {
                        // ✅ Khi user tự click vào input lần đầu tiên
                        hasFocusedStudentPageInput = true;
                    });


            pageInput.addEventListener('keydown', function(event) {
                if (event.key === 'Enter') {
                    const value = parseInt(pageInput.value);
                    if (isNaN(value) || value < 1 || value > totalPages) {
                        currentStudentPage = 0;
                        fetchStudents(0);
                    } else {
                        currentStudentPage = value - 1;
                        fetchStudents(currentStudentPage);
                    }
                }
            });
        }
    }, 0);
}

function attachStudentActionButtons() {
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function () {
            const studentId = this.getAttribute('data-id');
            window.location.href = `/studentDetail/${studentId}`;
        });
    });

    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function () {
            const studentId = this.getAttribute('data-id');
            showConfirmPopup('Bạn có chắc muốn xóa sinh viên này?', function () {
                fetch(`/student/deleteStudent/${studentId}`, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error('Xóa thất bại');
                        return response.text();
                    })
                    .then(message => {
                        console.log('✅ Xóa student thành công:', message);
                        fetchStudents(currentStudentPage);
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá student:', error);
                        alert('Đã xảy ra lỗi khi xoá sinh viên.');
                    });
            });
        });
    });
}

function prevStudentPage() {
    if (currentStudentPage > 0) {
        currentStudentPage--;
        fetchStudents(currentStudentPage);
    }
}

function nextStudentPage(totalPages) {
    if (currentStudentPage < totalPages - 1) {
        currentStudentPage++;
        fetchStudents(currentStudentPage);
    }
}

function handlePageInput(value, totalPages) {
    let page = parseInt(value) - 1;
    if (isNaN(page)) return;

    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    if (page !== currentStudentPage) {
        currentStudentPage = page;
        fetchStudents(page);
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function showLoading() {
    document.getElementById('loadingSpinner').style.display = 'block';
}
function hideLoading() {
    document.getElementById('loadingSpinner').style.display = 'none';
}

function debounce(func, delay) {
    let timer;
    return function(...args) {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delay);
    };
}

function showConfirmPopup(message, onConfirm) {
    Swal.fire({
        title: 'Xác nhận',
        text: message,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            onConfirm();
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    fetchStudents();
});
