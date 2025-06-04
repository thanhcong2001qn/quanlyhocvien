document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".search-form");
    const tableBody = document.querySelector(".table tbody");
    const nameInput = form.querySelector('input[name="name"]');
    const classInput = form.querySelector('input[name="className"]'); // ✅ Đã đổi
    const clearButton = form.querySelector(".clear-search");

    let timeout = null;

    function doSearch() {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData).toString();

        fetch("/student/search?" + params, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(res => res.text())
        .then(html => {
            tableBody.innerHTML = html;
        })
        .catch(err => console.error("❌ Search failed:", err));
    }

    // Chặn submit mặc định form (Enter)
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        doSearch();
    });

    // Gõ input sẽ tự động tìm kiếm
    [nameInput, classInput].forEach(input => {
        input.addEventListener("input", () => {
            clearTimeout(timeout);
            timeout = setTimeout(doSearch, 300);
        });
    });

    // ✅ Xử lý nút Xóa bộ lọc
    clearButton.addEventListener("click", () => {
        nameInput.value = "";
        classInput.value = "";

        // Xoá mọi query param tồn tại
        const url = new URL(window.location.origin + "/student/search");

        fetch(url.toString(), {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(res => res.text())
        .then(html => {
            tableBody.innerHTML = html;
        })
        .catch(err => console.error("❌ Search reset failed:", err));
    });
});
