let currentPage = 0;
const pageSize = 10;

// Hàm fetch tổng quát
async function fetchData(apiUrl, page = 0) {
    const response = await fetch(`${apiUrl}?page=${page}&size=${pageSize}`);
    const data = await response.json();
    return data; // Trả về dữ liệu
}

// Hàm riêng cho giáo viên
async function loadTeachers(page = 0) {
    const data = await fetchData('/api/teachers', page);
    renderTeacherTable(data.content);
    renderPagination(data.totalPages, page, loadTeachers);
}

// Hàm riêng cho sinh viên
async function loadStudents(page = 0) {
    const data = await fetchData('/api/students', page);
    renderStudentTable(data.content);
    renderPagination(data.totalPages, page, loadStudents);
}

// Render bảng giáo viên
function renderTeacherTable(teachers) {
    const tbody = document.querySelector("#teacherTable tbody");
    tbody.innerHTML = "";

    teachers.forEach(teacher => {
        const row = `<tr>
            <td>${teacher.teacherId}</td>
            <td>${teacher.account?.fullName || 'N/A'}</td>
            <td>${teacher.account?.gender || 'N/A'}</td>
            <td>${teacher.subjectSpecialization}</td>
            <td>${teacher.hireDate || 'N/A'}</td>
            <td><!-- Nút edit, xóa --></td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

// Render bảng sinh viên
function renderStudentTable(students) {
    const tbody = document.querySelector("#studentTable tbody");
    tbody.innerHTML = "";

    students.forEach(student => {
        const row = `<tr>
            <td>${student.id}</td>
            <td>${student.fullName}</td>
            <td>${student.gender}</td>
            <td>${student.className}</td>
            <td>${student.dateOfBirth || 'N/A'}</td>
            <td><!-- Nút edit, xóa --></td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

// Render phân trang
function renderPagination(totalPages, currentPage, loadFunction) {
    const pagination = document.querySelector(".pagination");
    pagination.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        pagination.innerHTML += `<li class="page-item ${i === currentPage ? 'active' : ''}">
            <button class="page-link" onclick="changePage(${i}, '${loadFunction.name}')">${i + 1}</button>
        </li>`;
    }
}

// Chuyển trang
function changePage(page, loadFunctionName) {
    currentPage = page;
    window[loadFunctionName](page); // Gọi lại đúng hàm load
}

// Khi load trang
document.addEventListener('DOMContentLoaded', () => {
    // Ví dụ:
    loadTeachers();
    // hoặc loadStudents();
});
