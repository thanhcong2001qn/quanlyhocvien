document.addEventListener('DOMContentLoaded', function() {
    // Xử lý thông báo dropdown
    const notificationBtn = document.querySelector('.notification-btn');
    const notificationDropdown = document.querySelector('.header-notification');

    if (notificationBtn) {
        notificationBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            notificationDropdown.classList.toggle('active');

            // Đóng profile nếu đang mở
            const profileDropdown = document.querySelector('.header-profile');
            if (profileDropdown && profileDropdown.classList.contains('active')) {
                profileDropdown.classList.remove('active');
            }
        });
    }

    // Xử lý profile dropdown
    const profileBtn = document.querySelector('.profile-btn');
    const profileDropdown = document.querySelector('.header-profile');

    if (profileBtn) {
        profileBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            profileDropdown.classList.toggle('active');

            // Đóng notification nếu đang mở
            if (notificationDropdown && notificationDropdown.classList.contains('active')) {
                notificationDropdown.classList.remove('active');
            }
        });
    }

    // Đóng dropdown khi click ra ngoài
    document.addEventListener('click', function() {
        if (notificationDropdown) {
            notificationDropdown.classList.remove('active');
        }
        if (profileDropdown) {
            profileDropdown.classList.remove('active');
        }
    });

    // Ngăn chặn đóng dropdown khi click vào nội dung bên trong
    const dropdowns = document.querySelectorAll('.notification-dropdown, .profile-dropdown');
    dropdowns.forEach(dropdown => {
        if (dropdown) {
            dropdown.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        }
    });

    // Xử lý nút toggle menu trên mobile
    const mobileToggle = document.querySelector('.mobile-toggle');

    if (mobileToggle) {
        mobileToggle.addEventListener('click', function() {
            document.body.classList.toggle('sidebar-open');

            // Đóng cả notification và profile khi mở sidebar mobile
            if (notificationDropdown) {
                notificationDropdown.classList.remove('active');
            }
            if (profileDropdown) {
                profileDropdown.classList.remove('active');
            }
        });
    }

    document.addEventListener('click', function(event) {
        // Kiểm tra nếu sidebar đang mở
        if (document.body.classList.contains('sidebar-open')) {
            const sidebar = document.querySelector('.sidebar'); // Điều chỉnh selector này cho phù hợp với class sidebar của bạn
            const mobileToggleElement = document.querySelector('.mobile-toggle'); // Điều chỉnh selector này cho phù hợp

            // Nếu click không phải trên sidebar và không phải trên nút toggle
            if (sidebar && !sidebar.contains(event.target) &&
                mobileToggleElement && !mobileToggleElement.contains(event.target)) {
                document.body.classList.remove('sidebar-open');
            }
        }
    });
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) {
        sidebar.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    }

});