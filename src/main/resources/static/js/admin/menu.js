document.addEventListener('DOMContentLoaded', function() {
    const menuItems = document.querySelectorAll('.menu-item');

    // Thêm chỉ số cho từng mục con để tạo hiệu ứng lệch thời gian
    menuItems.forEach(item => {
        const submenuItems = item.querySelectorAll('.submenu li');
        submenuItems.forEach((subItem, index) => {
            subItem.style.setProperty('--item-index', index);
        });
    });

    menuItems.forEach(item => {
        const link = item.querySelector('.menu-link');
        if (link) {
            link.addEventListener('click', function(e) {

                    // Nếu menu này đang mở, đóng nó một cách mượt mà
                if (item.classList.contains('show')) {
                    closeSubmenuSmoothly(item);
                } else {
                    // Đóng tất cả menu khác trước khi mở menu này
                    menuItems.forEach(otherItem => {
                        if (otherItem !== item && otherItem.classList.contains('show')) {
                            closeSubmenuSmoothly(otherItem);
                        }
                    });

                    // Mở menu này
                    item.classList.add('show');
                }
            });
        }
    });

    // Đóng menu khi click ra ngoài
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.sidebar')) {
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

        // Trước tiên, thêm class cho animation đóng
        menuItem.classList.add('closing');

        // Lấy submenu của menuItem
        const submenu = menuItem.querySelector('.submenu');
        if (!submenu) return;

        // Animate các mục submenu trước
        const submenuItems = menuItem.querySelectorAll('.submenu li');
        const submenuItemsLength = submenuItems.length;

        submenuItems.forEach((item, index) => {
            const delay = (submenuItemsLength - index - 1) * 30; // Giảm delay xuống để animation nhanh hơn
            setTimeout(() => {
                item.style.opacity = '0';
                item.style.transform = 'translateY(-5px)';
            }, delay);
        });

        // Sau đó mới animate cả submenu
        setTimeout(() => {
            submenu.style.opacity = '0';
            submenu.style.transform = 'translateY(-10px)';

            // Cuối cùng mới xóa class show
            setTimeout(() => {
                menuItem.classList.remove('show');
                menuItem.classList.remove('closing');

                // Reset styles after animation completes
                submenuItems.forEach(item => {
                    item.removeAttribute('style');
                });
                submenu.removeAttribute('style');
            }, 200); // Giảm thời gian chờ để đóng nhanh hơn
        }, submenuItemsLength * 20); // Giảm thời gian tổng thể
    }
});