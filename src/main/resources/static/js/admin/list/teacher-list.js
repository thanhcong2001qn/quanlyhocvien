// public/js/admin/teacher/teachers-list.js

let currentTeacherPage = 0;
const teacherPageSize = 5;
let hasFocusedTeacherPageInput = false;
const debouncedHandleTeacherPageInput = debounce(handleTeacherPageInput, 400);

async function fetchTeachers(page = 0) {
    try {
        showLoading();
        const response = await fetch(`/teacher/api/teachers?page=${page}&size=${teacherPageSize}`);
        if (!response.ok) {
            throw new Error('Lỗi khi fetch teachers');
        }
        const data = await response.json();
        renderTeacherTable(data.content);
        renderTeacherPagination(data.totalPages, page);
    } catch (error) {
        console.error('❌ Lỗi fetch teachers:', error);
    } finally {
        hideLoading();
    }
}

function renderTeacherTable(teachers) {
    const tbody = document.querySelector(".table tbody");
    tbody.innerHTML = "";

    teachers.forEach(teacher => {
        const genderBadge = teacher.gender === 'Nam' ? 'male' : 'female';

        const row = `<tr>
            <td>${teacher.teacherId}</td>
            <td>${teacher.fullName || 'N/A'}</td>
            <td>
                <span class="gender-badge ${genderBadge}">
                    ${teacher.gender || 'N/A'}
                </span>
            </td>
            <td>${teacher.subjectSpecialization || 'N/A'}</td>
            <td>${teacher.hireDate ? formatDate(teacher.hireDate) : 'N/A'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-icon btn-edit" data-id="${teacher.teacherId}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-icon btn-delete" data-id="${teacher.teacherId}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </td>
        </tr>`;

        tbody.innerHTML += row;
    });

    attachTeacherActionButtons();
}

function renderTeacherPagination(totalPages, currentPage) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = `
        <div class="pagination-wrapper d-flex align-items-center justify-content-center gap-3">
            <button class="btn btn-pagination" onclick="prevPage()" ${currentPage === 0 ? 'disabled' : ''}>
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

            <button class="btn btn-pagination" onclick="nextPage(${totalPages})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;

    setTimeout(() => {
        const pageInput = document.getElementById('pageInput');
                if (pageInput) {
                    if (hasFocusedTeacherPageInput) {
                        // ✅ Chỉ focus nếu người dùng đã từng focus trước đó
                        pageInput.focus();
                        pageInput.select();
                    }

                    pageInput.addEventListener('focus', function () {
                        // ✅ Khi user tự click vào input lần đầu tiên
                        hasFocusedTeacherPageInput = true;
                    });


            pageInput.addEventListener('keydown', function(event) {
                if (event.key === 'Enter') {
                    const value = parseInt(pageInput.value);
                    if (isNaN(value) || value < 1 || value > totalPages) {
                        currentPage = 0;
                        fetchTeachers(0);
                    } else {
                        currentPage = value - 1;
                        fetchTeachers(currentPage);
                    }
                }
            });
        }
    }, 0);
}

function attachTeacherActionButtons() {
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function () {
            const teacherId = this.getAttribute('data-id');
            window.location.href = `/teacherDetail/${teacherId}`;
        });
    });

    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function () {
            const teacherId = this.getAttribute('data-id');
            showConfirmPopup('Bạn có chắc muốn xóa giáo viên này?', function () {
                fetch(`/teacher/deleteTeacher/${teacherId}`, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error('Xóa thất bại');
                        return response.text();
                    })
                    .then(message => {
                        console.log('✅ Xóa giáo viên thành công:', message);
                        fetchTeachers(currentTeacherPage);
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá giáo viên:', error);
                        alert('Đã xảy ra lỗi khi xoá giáo viên.');
                    });
            });
        });
    });
}

function prevTeacherPage() {
    if (currentTeacherPage > 0) {
        currentTeacherPage--;
        fetchTeachers(currentTeacherPage);
    }
}

function nextTeacherPage(totalPages) {
    if (currentTeacherPage < totalPages - 1) {
        currentTeacherPage++;
        fetchTeachers(currentTeacherPage);
    }
}

function handleTeacherPageInput(value, totalPages) {
    let page = parseInt(value) - 1;
    if (isNaN(page)) return;
    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    if (page !== currentTeacherPage) {
        currentTeacherPage = page;
        fetchTeachers(page);
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
    return function (...args) {
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

// Khi trang load xong
document.addEventListener('DOMContentLoaded', () => {
    fetchTeachers();
});
