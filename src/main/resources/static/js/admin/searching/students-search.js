document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".search-form");
    const tableBody = document.querySelector(".table tbody");
    const nameInput = form.querySelector('input[name="name"]');
    const classInput = form.querySelector('input[name="class"]');
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

    // Gõ input vào sẽ tự tìm kiếm sau 300ms
    [nameInput, classInput].forEach(input => {
        input.addEventListener("input", () => {
            clearTimeout(timeout);
            timeout = setTimeout(doSearch, 300);
        });
    });

    // Click nút "Xóa bộ lọc" để reset form và tìm kiếm lại
    clearButton.addEventListener("click", () => {
        form.reset();
        doSearch();
    });
});
