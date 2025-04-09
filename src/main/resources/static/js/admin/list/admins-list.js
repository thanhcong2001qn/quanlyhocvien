// public/js/admin/admins-list.js

let currentAdminPage = 0;
const adminPageSize = 5;
let hasFocusedAccountPageInput = false;
const debouncedHandlePageInput = debounce(handlePageInput, 400);

async function fetchAdmins(page = 0) {
    try {
        showLoading();
        const response = await fetch(`/admin/api/admins?page=${page}&size=${adminPageSize}`);
        if (!response.ok) {
            throw new Error('Lỗi khi fetch admins');
        }
        const data = await response.json();
        renderAdminTable(data.content);
        renderAdminPagination(data.totalPages, page);
        attachAdminActionButtons();
    } catch (error) {
        console.error('❌ Lỗi fetch admin:', error);
    } finally {
        hideLoading();
    }
}


function renderAdminTable(admins) {
    const tbody = document.querySelector("#adminTable tbody");
    tbody.innerHTML = "";

    admins.forEach(admin => {
        const row = `<tr>
            <td>${admin.adminId}</td>
            <td>${admin.fullName || 'N/A'}</td>
            <td>${admin.email || 'N/A'}</td>
            <td>${admin.roleName || 'N/A'}</td>
            <td>${admin.createdAt ? formatDate(admin.createdAt) : 'N/A'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-icon btn-edit" data-id="${admin.adminId}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-icon btn-delete" data-id="${admin.adminId}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function renderAdminPagination(totalPages, currentPage) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = `
        <div class="pagination-wrapper d-flex align-items-center justify-content-center gap-3">
            <button class="btn btn-pagination" onclick="prevAdminPage()" ${currentPage === 0 ? 'disabled' : ''}>
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

            <button class="btn btn-pagination" onclick="nextAdminPage(${totalPages})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;

    setTimeout(() => {
        const pageInput = document.getElementById('pageInput');
        if (pageInput) {
            if (hasFocusedAccountPageInput) {
                // ✅ Focus lại nếu người dùng đã từng focus
                pageInput.focus();
                pageInput.select();  // ✅ Chọn toàn bộ nội dung để người dùng dễ sửa
            }

            pageInput.addEventListener('focus', function () {
                hasFocusedAccountPageInput = true;
            });

            pageInput.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    const value = parseInt(pageInput.value);
                    if (isNaN(value) || value < 1 || value > totalPages) {
                        currentAdminPage = 0;
                        fetchAdmins(0);
                    } else {
                        currentAdminPage = value - 1;
                        fetchAdmins(currentAdminPage);
                    }
                    // ✅ Sau khi fetch xong, sẽ tự focus lại do code ở trên
                }
            });
        }
    }, 0);
}


function changeAdminPage(page) {
    currentAdminPage = page;
    fetchAdmins(page);
}

function attachAdminActionButtons() {
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function () {
            const adminId = this.getAttribute('data-id');
            window.location.href = `/adminDetail/${adminId}`;
        });
    });

    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function () {
            const adminId = this.getAttribute('data-id');

            showConfirmPopup('Bạn có chắc muốn xóa admin này?', function () {
                // ✅ Thực hiện gọi API xoá admin tại đây
                fetch(`/admin/deleteAdmin/${adminId}`, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error('Xóa thất bại');
                        return response.text();
                    })
                    .then(message => {
                        console.log('✅ Xóa admin thành công:', message);
                        fetchAdmins(currentAdminPage); // Load lại bảng admin sau khi xoá
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá admin:', error);
                        alert('Đã xảy ra lỗi khi xoá admin.');
                    });
            });
        });
    });
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

document.addEventListener('DOMContentLoaded', () => {
    rebindSidebarEvents();
    fetchAdmins();
});

function rebindSidebarEvents() {
    const sidebar = document.querySelector('.sidebar');
    const menuItems = document.querySelectorAll('.menu-item');

    if (!sidebar || !menuItems.length) return;

    menuItems.forEach(item => {
        const link = item.querySelector('.menu-link');
        if (link) {
            const newLink = link.cloneNode(true);
            link.parentNode.replaceChild(newLink, link);

            newLink.addEventListener('click', function (e) {
                const href = newLink.getAttribute('href');
                if (href === '#' || href === null) {
                    e.preventDefault();
                }

                if (!sidebar.classList.contains('collapsed')) {
                    if (item.classList.contains('show')) {
                        closeSubmenuSmoothly(item);
                    } else {
                        menuItems.forEach(otherItem => {
                            if (otherItem !== item && otherItem.classList.contains('show')) {
                                closeSubmenuSmoothly(otherItem);
                            }
                        });
                        item.classList.add('show');
                    }
                }
            });
        }
    });
}

function prevAdminPage() {
    if (currentAdminPage > 0) {
        currentAdminPage--;
        fetchAdmins(currentAdminPage);
    }
}

function nextAdminPage(totalPages) {
    if (currentAdminPage < totalPages - 1) {
        currentAdminPage++;
        fetchAdmins(currentAdminPage);
    }
}

function goToAdminPage(pageInputValue) {
    let page = parseInt(pageInputValue) - 1;
    if (!isNaN(page) && page >= 0 && page < totalAdminPages) {
        if (page !== currentAdminPage) { // Chỉ fetch nếu khác trang hiện tại
            currentAdminPage = page;
            fetchAdmins(page);
        }
    }
}

function handlePageInput(value, totalPages) {
    let page = parseInt(value) - 1;
    if (isNaN(page)) return;

    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    if (page !== currentAdminPage) {
        currentAdminPage = page;
        fetchAdmins(page);
    }
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


function closeSubmenuSmoothly(menuItem) {
    if (!menuItem) return;
    menuItem.classList.add('closing');
    const submenu = menuItem.querySelector('.submenu');
    if (!submenu) return;

    submenu.style.opacity = '0';
    submenu.style.transform = 'translateY(-10px)';
    setTimeout(() => {
        menuItem.classList.remove('show');
        menuItem.classList.remove('closing');
        submenu.removeAttribute('style');
    }, 400);
}

// ✅ Hàm Confirm Popup đơn giản
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