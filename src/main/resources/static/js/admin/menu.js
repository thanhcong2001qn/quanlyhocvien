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

    applySidebarState();

    function addTransitionClass() {
        sidebar.classList.add('with-transition');
        setTimeout(() => {
            sidebar.classList.remove('with-transition');
        }, 300);
    }

    sidebarToggleBtn.addEventListener('click', function(event) {
        event.stopPropagation();
        addTransitionClass();
        sidebar.classList.toggle('collapsed');
        document.body.classList.toggle('sidebar-collapsed');

        localStorage.setItem(
            'sidebarState',
            sidebar.classList.contains('collapsed') ? 'collapsed' : 'expanded'
        );

        if (sidebar.classList.contains('collapsed')) {
            menuItems.forEach(item => item.classList.remove('show'));
        }
    });

    menuItems.forEach(item => {
        const link = item.querySelector('.menu-link');
        if (!link) return;

        link.addEventListener('click', function(e) {
            const isAnchor = link.tagName.toLowerCase() === 'a';
            const href = link.getAttribute('href');

            // Ngăn reload nếu không có href hoặc chỉ là toggle
           if (!isAnchor || !href || href === '#') {
                   e.preventDefault();
                   e.stopPropagation(); // ✅ giữ lại
               } else {
                   // Nếu là link thật thì không làm gì cả
                   return;
               }

            // Nếu menu không bị collapsed
            if (!sidebar.classList.contains('collapsed')) {
                const submenu = item.querySelector('.submenu');
                if (!submenu) return;

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
    });

    document.addEventListener('click', function(e) {
        if (!e.target.closest('.sidebar') && !e.target.closest('#toggle-sidebar')) {
            menuItems.forEach(item => {
                if (item.classList.contains('show')) {
                    closeSubmenuSmoothly(item);
                }
            });
        }
    });

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
