document.addEventListener('DOMContentLoaded', function() {
    displayUserProfile();
    logout();
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
            const sidebar = document.querySelector('.sidebar');
            const mobileToggleElement = document.querySelector('.mobile-toggle');

            // Nếu click không phải trên sidebar và không phải trên nút toggle
            if (sidebar && !sidebar.contains(event.target) &&
                mobileToggleElement && !mobileToggleElement.contains(event.target)) {
                document.body.classList.remove('sidebar-open');
            }
        }
    });

    const sidebar = document.querySelector('.sidebar');
    const headerDivider = document.querySelector('.header-divider');

    // Xử lý toggle sidebar và border-bottom
    const sidebarToggleBtn = document.getElementById('toggle-sidebar');

    // Thay đổi phần này trong event listener của sidebarToggleBtn
    if (sidebarToggleBtn && sidebar && headerDivider) {
        sidebarToggleBtn.addEventListener('click', function(event) {
            event.stopPropagation();

            // Toggle sidebar state
            sidebar.classList.toggle('collapsed');
            document.body.classList.toggle('sidebar-collapsed');

            // Store sidebar state
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('sidebarState', isCollapsed ? 'collapsed' : 'expanded');

            // Không cần thiết lập width trực tiếp nữa vì đã xử lý bằng CSS
        });
    }

    // Xóa phần xử lý width trong window resize event
    window.addEventListener('resize', function() {
        // Không cần thiết lập width trực tiếp nữa
    });

    // Apply saved sidebar state on page load
    const savedState = localStorage.getItem('sidebarState');
    if (savedState === 'collapsed' && sidebar && headerDivider) {
        sidebar.classList.add('collapsed');
        document.body.classList.add('sidebar-collapsed');
        headerDivider.style.width = 'calc(100% - 70px)';
    }



    if (sidebar) {
        sidebar.addEventListener('click', function(event) {
            event.stopPropagation();
        });
    }
});
function displayUserProfile() {
    const username = localStorage.getItem('username');

    // Get the profile name element
    const profileNameElement = document.querySelector('.profile-name');

    // Update the profile name if username exists in localStorage
    if (username) {
        profileNameElement.textContent = username;
    } else {
        // If no username in localStorage, keep default or set a placeholder
        profileNameElement.textContent = 'Guest User';

        // Optionally, you could set a default username in localStorage
        // localStorage.setItem('username', 'Guest User');
    }

    // Optional: Handle saving username to localStorage
    // This would be used elsewhere in your app when setting the username
    function saveUsername(name) {
        localStorage.setItem('username', name);
        profileNameElement.textContent = name;
    }

    // Make this function available globally if needed
    window.saveUsername = saveUsername;
}
function logout() {
    const logoutButton = document.querySelector('.logout-btn');

    // Add click event listener to the logout button
    logoutButton.addEventListener('click', function(event) {
        // Prevent default behavior of the button
        event.preventDefault();

        // Clear user data from localStorage
        localStorage.clear();


        // Redirect to login page or home page
        window.location.href = '/login'; // Change this to your login page URL
    });

    // Optional: Toggle dropdown visibility when profile button is clicked
    const profileBtn = document.querySelector('.profile-btn');
    const profileDropdown = document.querySelector('.profile-dropdown');

    if (profileBtn && profileDropdown) {
        profileBtn.addEventListener('click', function() {
            profileDropdown.classList.toggle('show');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!event.target.closest('.header-profile')) {
                profileDropdown.classList.remove('show');
            }
        });
    }
}