document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("searchForm");
    const tableBody = document.querySelector(".table tbody");
    const keywordInput = document.getElementById("searchInput");

    // Hàm gửi AJAX và cập nhật bảng
    function doSearch() {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData).toString();

        fetch("/account/accounts?" + params, {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(response => response.text())
        .then(html => {
            tableBody.innerHTML = html;
        })
        .catch(err => console.error("AJAX error:", err));
    }

    // Sự kiện submit form
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        doSearch();
    });

    // Tự động submit khi chọn role hoặc status
    document.getElementById("roleFilter").addEventListener("change", doSearch);
    document.getElementById("statusFilter").addEventListener("change", doSearch);

    // Gõ vào ô từ khóa → delay rồi tìm kiếm
    let timeout = null;
    keywordInput.addEventListener("input", function () {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            doSearch();
        }, 300); // Đợi 300ms sau khi ngừng gõ
    });

    // Nút "Xóa bộ lọc"
    document.querySelector(".clear-search").addEventListener("click", () => {
        form.reset();
        doSearch();
    });
});
