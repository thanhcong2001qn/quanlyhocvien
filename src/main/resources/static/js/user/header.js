
let isSearchActive = false;

document.addEventListener('DOMContentLoaded', function () {
    updateCartBadge();
    updateAuthUI();
    updateUtcTime();
    active();
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
    showConfirmation("Bạn có chắc muốn đăng xuất?", function (){
        localStorage.clear();
        // Cập nhật giao diện
        updateAuthUI();

        // Chuyển hướng đến trang chủ
        window.location.href = '/home';

        // Ghi log thời gian đăng xuất
        //const now = new Date();
        //console.log(`Đăng xuất thành công lúc: ${formatUTCDateTime(now)}`);
    }, function () {
        // Không làm gì cả
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

function checkRoleBeforeNavigate(url) {
    // Lấy token từ localStorage
    const token = localStorage.getItem('token');

    if (!token) {
        console.error('Không tìm thấy token!');
        alert('Bạn chưa đăng nhập! Sẽ chuyển hướng đến trang đăng nhập.');
        window.location.href = '/login';
        return;
    }

    // Phân tích JWT token (không cần thư viện)
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const payload = JSON.parse(jsonPayload);
        console.log('Token payload:', payload);

        if (payload.roles) {
            console.log('User roles:', payload.roles);
            alert('Roles của bạn: ' + payload.roles + '\nUsername: ' + payload.sub);

            // Kiểm tra role
            if (payload.roles.includes('ROLE_STUDENT')) {
                console.log('Bạn có quyền truy cập trang này!');
                // Thay vì cách cũ
                // window.location.href = url;

                // Sử dụng fetch với token
                fetch(url, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else if (response.ok) {
                            // Xử lý HTML response và thay thế nội dung trang
                            response.text().then(html => {
                                document.open();
                                document.write(html);
                                document.close();
                                history.pushState({}, '', url);
                            });
                        }
                    });
            } else {
                alert('Bạn không có quyền truy cập trang này!');
            }
        } else {
            console.error('Token không chứa thông tin role!');
            alert('Token không chứa thông tin role!');
        }
    } catch (e) {
        console.error('Lỗi khi phân tích token:', e);
        alert('Token không hợp lệ!');
    }
}