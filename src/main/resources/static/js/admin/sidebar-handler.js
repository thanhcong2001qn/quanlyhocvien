function rebindSidebarEvents() {
    const sidebar = document.querySelector('.sidebar');

    if (!sidebar) return;

    sidebar.addEventListener('click', function (e) {
        const link = e.target.closest('.menu-link');
        if (!link) return;

        const item = link.closest('.menu-item');
        const href = link.getAttribute('href');

        if (!href || href === '#') {
            e.preventDefault();

            if (!sidebar.classList.contains('collapsed')) {
                const isShown = item.classList.contains('show');

                document.querySelectorAll('.menu-item.show').forEach(other => {
                    if (other !== item) {
                        closeSubmenuSmoothly(other);
                    }
                });

                if (isShown) {
                    closeSubmenuSmoothly(item);
                } else {
                    item.classList.add('show');
                }
            }
        }
    });
}

function closeSubmenuSmoothly(menuItem) {
    if (!menuItem) return;
    menuItem.classList.add('closing');
    const submenu = menuItem.querySelector('.submenu');
    if (!submenu) return;

    submenu.style.opacity = '0';
    submenu.style.transform = 'translateY(-10px)';
    setTimeout(() => {
        menuItem.classList.remove('show');
        menuItem.classList.remove('closing');
        submenu.removeAttribute('style');
    }, 400);
}

// 👇 Bắt đầu sau khi DOM load xong
document.addEventListener('DOMContentLoaded', function () {
    rebindSidebarEvents();
});
