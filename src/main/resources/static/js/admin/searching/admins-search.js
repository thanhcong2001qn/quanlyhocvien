// public/js/admin/searching/admins-search.js

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("searchForm");
    const tableBody = document.querySelector("#adminTable tbody");
    const nameInput = form.querySelector('input[name="name"]');
    const roleInput = form.querySelector('input[name="role"]');
    const clearButton = document.querySelector(".clear-search");

    let timeout = null;

    function doSearch() {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData).toString();

        fetch("/admin/search?" + params, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(res => res.text())
        .then(html => {
            tableBody.innerHTML = html;
        })
        .catch(err => console.error("Search failed:", err));
    }

    // Chặn submit form mặc định
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        doSearch();
    });

    // Gõ input thì tự động tìm kiếm (debounce 300ms)
    [nameInput, roleInput].forEach(input => {
        input.addEventListener("input", () => {
            clearTimeout(timeout);
            timeout = setTimeout(doSearch, 300);
        });
    });

    // Xóa bộ lọc
    clearButton.addEventListener("click", () => {
        form.reset();
        doSearch();
    });
});
