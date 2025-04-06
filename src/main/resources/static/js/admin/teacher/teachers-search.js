document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("searchForm");
    const tableBody = document.querySelector(".table tbody");
    const nameInput = document.getElementById("searchName");
    const subjectInput = document.getElementById("searchSubject");
    const clearButton = document.querySelector(".clear-search");

    let timeout = null;

    function doSearch() {
        const formData = new FormData(form);
        const params = new URLSearchParams(formData).toString();

        fetch("/teacher/search?" + params, {
            method: "GET",
            headers: { "X-Requested-With": "XMLHttpRequest" }
        })
        .then(res => res.text())
        .then(html => {
            tableBody.innerHTML = html;
        })
        .catch(err => console.error("Search failed:", err));
    }

    // Chặn submit mặc định
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        doSearch();
    });

    // Gõ input thì tự tìm
    [nameInput, subjectInput].forEach(input => {
        input.addEventListener("input", () => {
            clearTimeout(timeout);
            timeout = setTimeout(doSearch, 300);
        });
    });

    // Nút xóa bộ lọc
    clearButton.addEventListener("click", () => {
        form.reset();
        doSearch();
    });
});
