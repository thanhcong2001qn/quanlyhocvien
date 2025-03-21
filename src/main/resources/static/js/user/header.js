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
document.addEventListener('DOMContentLoaded', function () {
    updateAuthUI();
    updateUtcTime();

    // Cập nhật thời gian UTC mỗi phút
    setInterval(updateUtcTime, 60000);

    // Lắng nghe sự kiện storage
    window.addEventListener('storage', function (e) {
        if (e.key === 'token' || e.key === 'username') {
            updateAuthUI();
        }
    });
});

function updateAuthUI() {
    const token = localStorage.getItem('token');
    const notAuthMenu = document.getElementById('not-authenticated-menu');
    const authMenu = document.getElementById('authenticated-menu');
    const usernameDisplay = document.getElementById('username-display');

    if (token && !isTokenExpired(token)) {
        // Đã đăng nhập
        notAuthMenu.style.display = 'none';
        authMenu.style.display = 'block';

        // Hiển thị username
        const userInfo = getUserInfoFromToken(token);
        usernameDisplay.textContent = userInfo.username || 'Người dùng';
    } else {
        // Chưa đăng nhập
        notAuthMenu.style.display = 'block';
        authMenu.style.display = 'none';
    }
}

function updateUtcTime() {
    const utcTimeDisplay = document.getElementById('utc-time-display');
    if (utcTimeDisplay) {
        const now = new Date();
        const utcTime = formatUTCDateTime(now);
        utcTimeDisplay.textContent = utcTime;
    }
}

function formatUTCDateTime(date) {
    const pad = (num) => num.toString().padStart(2, '0');

    const year = date.getUTCFullYear();
    const month = pad(date.getUTCMonth() + 1);
    const day = pad(date.getUTCDate());
    const hours = pad(date.getUTCHours());
    const minutes = pad(date.getUTCMinutes());
    const seconds = pad(date.getUTCSeconds());

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000; // Convert to milliseconds
        return Date.now() >= exp;
    } catch (e) {
        return true; // Nếu có lỗi, coi như token đã hết hạn
    }
}

function getUserInfoFromToken(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return {
            id: payload.id,
            username: payload.sub, // JWT thường lưu username trong claim 'sub'
            roles: payload.roles || []
        };
    } catch (e) {
        console.error('Lỗi khi đọc thông tin từ token:', e);
        return {};
    }
}

function logout() {
    // Hiển thị xác nhận nếu cần
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        // Xóa token và thông tin người dùng
        localStorage.removeItem('token');
        // localStorage.removeItem('refresh_token');
        localStorage.removeItem('username');

        // Cập nhật giao diện
        updateAuthUI();

        // Chuyển hướng đến trang chủ
        window.location.href = '/home';

        // Ghi log thời gian đăng xuất
        //const now = new Date();
        //console.log(`Đăng xuất thành công lúc: ${formatUTCDateTime(now)}`);
    }
}