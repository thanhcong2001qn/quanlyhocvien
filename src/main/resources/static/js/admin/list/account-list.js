// public/js/admin/list/accounts-list.js

let currentAccountPage = 0;
const accountPageSize = 5;
let hasFocusedAccountPageInput = false;
const debouncedHandlePageInput = debounce(handlePageInput, 400);

async function fetchAccounts(page = 0) {
    try {
        showLoading();
        const response = await fetch(`/account/api/accounts?page=${page}&size=${accountPageSize}`);
        if (!response.ok) {
            throw new Error('Lỗi khi fetch accounts');
        }
        const data = await response.json();
        renderAccountTable(data.content);
        renderAccountPagination(data.totalPages, page);
        attachAccountActionButtons();
    } catch (error) {
        console.error('❌ Lỗi fetch account:', error);
    } finally {
        hideLoading();
    }
}

function renderAccountTable(accounts) {
    const tbody = document.querySelector(".table tbody");
    tbody.innerHTML = "";

    accounts.forEach(account => {
        const row = `<tr>
            <td>${account.accountId}</td>
            <td>${account.username || 'N/A'}</td>
            <td>${account.fullName || 'N/A'}</td>
            <td>${account.email || 'N/A'}</td>
            <td>${account.roleName || 'N/A'}</td>
            <td>
                <span class="status-badge ${account.isActive ? 'status-active' : 'status-inactive'}">
                    ${account.isActive ? 'Hoạt động' : 'Đã khóa'}
                </span>
            </td>
            <td>${account.createdAt ? formatDate(account.createdAt) : 'N/A'}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-icon btn-edit" data-id="${account.accountId}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-icon btn-delete" data-id="${account.accountId}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

function renderAccountPagination(totalPages, currentPage) {
    const pagination = document.querySelector(".pagination"); // lấy class pagination trong card-footer
    pagination.innerHTML = `
        <div class="pagination-wrapper d-flex align-items-center justify-content-center gap-3">
            <button class="btn btn-pagination" onclick="prevAccountPage()" ${currentPage === 0 ? 'disabled' : ''}>
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

            <button class="btn btn-pagination" onclick="nextAccountPage(${totalPages})" ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;

    setTimeout(() => {
        const pageInput = document.getElementById('pageInput');
        if (pageInput) {
            if (hasFocusedAccountPageInput) {
                // ✅ Chỉ focus nếu người dùng đã từng focus trước đó
                pageInput.focus();
                pageInput.select();
            }

            pageInput.addEventListener('focus', function () {
                // ✅ Khi user tự click vào input lần đầu tiên
                hasFocusedAccountPageInput = true;
            });

            pageInput.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    const value = parseInt(pageInput.value);
                    if (isNaN(value) || value < 1 || value > totalPages) {
                        currentAccountPage = 0;
                        fetchAccounts(0);
                    } else {
                        currentAccountPage = value - 1;
                        fetchAccounts(currentAccountPage);
                    }
                }
            });
        }
    }, 0);
}

function changeAccountPage(page) {
    currentAccountPage = page;
    fetchAccounts(page);
}

function attachAccountActionButtons() {
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function () {
            const accountId = this.getAttribute('data-id');
            window.location.href = `/accountDetail/${accountId}`;
        });
    });

    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function () {
            const accountId = this.getAttribute('data-id');
            showConfirmPopup('Bạn có chắc muốn xóa tài khoản này?', function () {
                fetch(`/account/deleteAccount/${accountId}`, { method: 'DELETE' })
                    .then(response => {
                        if (!response.ok) throw new Error('Xóa thất bại');
                        return response.text();
                    })
                    .then(message => {
                        console.log('✅ Xóa account thành công:', message);
                        fetchAccounts(currentAccountPage);
                    })
                    .catch(error => {
                        console.error('❌ Lỗi khi xoá account:', error);
                        alert('Đã xảy ra lỗi khi xoá tài khoản.');
                    });
            });
        });
    });
}

function prevAccountPage() {
    if (currentAccountPage > 0) {
        currentAccountPage--;
        fetchAccounts(currentAccountPage);
    }
}

function nextAccountPage(totalPages) {
    if (currentAccountPage < totalPages - 1) {
        currentAccountPage++;
        fetchAccounts(currentAccountPage);
    }
}

function handlePageInput(value, totalPages) {
    let page = parseInt(value) - 1;
    if (isNaN(page)) return;

    if (page < 0) page = 0;
    if (page >= totalPages) page = totalPages - 1;

    if (page !== currentAccountPage) {
        currentAccountPage = page;
        fetchAccounts(page);
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

document.addEventListener('DOMContentLoaded', () => {
    fetchAccounts();
});
