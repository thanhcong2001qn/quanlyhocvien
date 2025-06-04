document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("searchForm");
    const tableBody = document.querySelector(".table tbody");
    const searchInput = document.getElementById("searchInput");
    const roleFilter = document.getElementById("roleFilter");
    const statusFilter = document.getElementById("statusFilter");
    const clearButton = document.querySelector(".clear-search");

    let timeout = null;

    function doSearch() {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData).toString();

        fetch("/account/search?" + params, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
            .then(res => res.text())
            .then(html => {
                tableBody.innerHTML = html;

                // Re-initialize Bootstrap tooltips after updating DOM
                const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                tooltipTriggerList.forEach(function (el) {
                    new bootstrap.Tooltip(el);
                });
            })
            .catch(err => console.error("❌ Lỗi khi tìm kiếm tài khoản:", err));
    }

    // Ngăn form submit mặc định
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        doSearch();
    });

    // Gõ tìm kiếm tên/email/username
    searchInput.addEventListener("input", () => {
        clearTimeout(timeout);
        timeout = setTimeout(doSearch, 300);
    });

    // Thay đổi lọc vai trò
    roleFilter.addEventListener("change", () => {
        doSearch();
    });

    // Thay đổi lọc trạng thái
    statusFilter.addEventListener("change", () => {
        doSearch();
    });

    // Nút Xóa bộ lọc
    clearButton.addEventListener("click", () => {
        searchInput.value = "";
        roleFilter.selectedIndex = 0;
        statusFilter.selectedIndex = 0;
        doSearch();
    });
});
