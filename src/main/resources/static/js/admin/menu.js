document.addEventListener('DOMContentLoaded', function() {
    const sidebarToggleBtn = document.getElementById('toggle-sidebar');
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.content-wrapper');
    const menuItems = document.querySelectorAll('.menu-item');

    if (!sidebar || !sidebarToggleBtn || !mainContent) {
        console.error("Sidebar hoặc nút toggle không tìm thấy!");
        return;
    }

    function applySidebarState() {
        if (localStorage.getItem('sidebarState') === 'collapsed') {
            sidebar.classList.add('collapsed');
            document.body.classList.add('sidebar-collapsed');
        } else {
            sidebar.classList.remove('collapsed');
            document.body.classList.remove('sidebar-collapsed');
        }
    }

    applySidebarState(); // Áp dụng trạng thái sidebar ngay khi load trang

    // Thêm transition class khi cần thiết để tránh hiệu ứng giật
    function addTransitionClass() {
        sidebar.classList.add('with-transition');
        setTimeout(() => {
            sidebar.classList.remove('with-transition');
        }, 300); // Thời gian bằng với transition
    }

    sidebarToggleBtn.addEventListener('click', function(event) {
        event.stopPropagation();

        // Thêm class transition trước khi toggle
        addTransitionClass();

        sidebar.classList.toggle('collapsed');
        document.body.classList.toggle('sidebar-collapsed');

        // Lưu trạng thái sidebar vào localStorage
        if (sidebar.classList.contains('collapsed')) {
            localStorage.setItem('sidebarState', 'collapsed');
            menuItems.forEach(item => item.classList.remove('show'));
        } else {
            localStorage.setItem('sidebarState', 'expanded');
        }
    });

    // Xử lý mở/đóng menu con trong sidebar
    menuItems.forEach(item => {
        const link = item.querySelector('.menu-link');
        if (link) {
            link.addEventListener('click', function(e) {
                if (!sidebar.classList.contains('collapsed')) {
                    if (item.classList.contains('show')) {
                        closeSubmenuSmoothly(item);
                    } else {
                        menuItems.forEach(otherItem => {
                            if (otherItem !== item && otherItem.classList.contains('show')) {
                                closeSubmenuSmoothly(otherItem);
                            }
                        });

                        item.classList.add('show');
                    }
                }
            });
        }
    });

    // Đóng menu khi click ra ngoài
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.sidebar') && !e.target.closest('#toggle-sidebar')) {
            menuItems.forEach(item => {
                if (item.classList.contains('show')) {
                    closeSubmenuSmoothly(item);
                }
            });
        }
    });

    // Hàm đóng submenu một cách mượt mà
    function closeSubmenuSmoothly(menuItem) {
        if (!menuItem) return;

        menuItem.classList.add('closing');
        const submenu = menuItem.querySelector('.submenu');
        if (!submenu) return;

        const submenuItems = menuItem.querySelectorAll('.submenu li');
        const submenuItemsLength = submenuItems.length;

        submenuItems.forEach((item, index) => {
            const delay = (submenuItemsLength - index - 1) * 80;
            setTimeout(() => {
                item.style.opacity = '0';
                item.style.transform = 'translateY(-5px)';
            }, delay);
        });

        setTimeout(() => {
            submenu.style.opacity = '0';
            submenu.style.transform = 'translateY(-10px)';

            setTimeout(() => {
                menuItem.classList.remove('show');
                menuItem.classList.remove('closing');
                submenuItems.forEach(item => item.removeAttribute('style'));
                submenu.removeAttribute('style');
            }, 400);
        }, submenuItemsLength * 80);
    }
});