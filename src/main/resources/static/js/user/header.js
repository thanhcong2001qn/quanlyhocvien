
let isSearchActive = false;

document.addEventListener('DOMContentLoaded', function () {
    updateCartBadge();
    updateUtcTime();
    active();
    // Cập nhật thời gian UTC mỗi phút
    setInterval(updateUtcTime, 60000);

});

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
    showConfirmation("Bạn có chắc muốn đăng xuất?", function() {
        // Tạo form ẩn để thực hiện POST request đến /logout
        const form = document.createElement('form');
        form.method = 'post';
        form.action = '/logout';

        // Thêm CSRF token nếu bạn đang sử dụng CSRF protection
        // Nếu bạn đã tắt CSRF, có thể bỏ qua phần này
        /*
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrfHeader;
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
        */

        // Thêm form vào document và submit
        document.body.appendChild(form);


        // Submit form để gửi request đến server
        form.submit();

        // Lưu ý: Các dòng code bên dưới có thể không được thực thi
        // vì việc submit form sẽ chuyển hướng trang

        // Cập nhật giao diện (nếu cần)
        // updateAuthUI();

        // Ghi log thời gian đăng xuất
        // const now = new Date();
        // console.log(`Đăng xuất thành công lúc: ${formatUTCDateTime(now)}`);
    }, function() {
        // Không làm gì cả khi người dùng hủy đăng xuất
    });
}
function active(){
    const navLinks = document.querySelectorAll('.nav-link-header');
    const currentPath = window.location.pathname;
    navLinks.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath === currentPath) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

function updateCartBadge() {
    fetchWithAuth('/api/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartBadge = document.getElementById('cartBadge');
            if (cartBadge) {
                // Cập nhật số lượng
                cartBadge.textContent = data.count;

                // Hiển thị hoặc ẩn badge dựa trên số lượng
                if (data.count > 0) {
                    cartBadge.style.display = 'inline-block';
                } else {
                    cartBadge.style.display = 'none';
                }
            }
        })
        .catch(error => {
            console.error('Lỗi khi lấy số lượng giỏ hàng:', error);
        });
}
