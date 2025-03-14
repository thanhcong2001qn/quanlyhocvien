const searchBtn = document.getElementById('search-btn');
const searchForm = document.getElementById('search-form');
let isSearchActive = false;

searchBtn.addEventListener('click', function (e) {
    e.preventDefault(); // Ngăn chặn hành vi mặc định
    e.stopPropagation(); // Ngăn chặn sự kiện lan ra ngoài

    if (!isSearchActive) {
        // Mở thanh tìm kiếm
        searchForm.classList.remove('not-active');
        searchForm.classList.add('active');
        isSearchActive = true;
    } else {
        // Đóng thanh tìm kiếm
        searchForm.classList.add('not-active');
        searchForm.classList.remove('active');
        isSearchActive = false;
    }
});