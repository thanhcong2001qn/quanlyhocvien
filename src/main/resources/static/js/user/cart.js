/**
 * Cart Page JavaScript
 * Created by: thanhcong2001qn
 * Last Updated: 2025-04-25
 */

document.addEventListener('DOMContentLoaded', function() {
    // Load cart data when page loads
    loadCartItems();

    // Event listeners
    document.getElementById('clearCartBtn').addEventListener('click', clearCart);
    document.getElementById('checkoutBtn').addEventListener('click', proceedToCheckout);
    document.getElementById('applyPromoBtn').addEventListener('click', applyPromoCode);
});

/**
 * Format currency (VND)
 * @param {number} amount - Amount to format
 * @returns {string} - Formatted amount
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(amount);
}

/**
 * Load cart items from API
 */
function loadCartItems() {
    // Show loading state
    document.getElementById('cartItemsList').innerHTML = `
        <div class="cart-loading">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
            <p class="mt-2">Đang tải giỏ hàng...</p>
        </div>
    `;

    // Fetch cart data sử dụng fetchWithAuth
    fetchWithAuth('/api/cart')
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải giỏ hàng');
            }
            return response.json();
        })
        .then(response => {
            const { items, count, total } = response;

            // Update cart count in header
            if (window.cart && window.cart.updateBadge) {
                window.cart.updateBadge();
            }

            // Check if cart is empty
            if (items.length === 0) {
                showEmptyCart();
                return;
            }

            // Render cart items
            renderCartItems(items);

            // Update summary
            updateCartSummary(response);
        })
        .catch(error => {
            console.error('Error loading cart:', error);
            showNotification('Có lỗi xảy ra khi tải giỏ hàng', 'error');

            // Show empty cart on error
            showEmptyCart();
        });
}

/**
 * Show empty cart message
 */
function showEmptyCart() {
    document.getElementById('emptyCartMessage').classList.remove('d-none');
    document.getElementById('cartContent').classList.add('d-none');
}

/**
 * Render cart items in the list
 * @param {Array} items - Cart items from API
 */
function renderCartItems(items) {
    const cartItemsList = document.getElementById('cartItemsList');
    cartItemsList.innerHTML = '';

    // Get template
    const template = document.getElementById('cartItemTemplate');

    // Render each item
    items.forEach(item => {
        const clone = document.importNode(template.content, true);

        // Set item data
        const cartItem = clone.querySelector('.cart-item');
        cartItem.dataset.id = item.courseId;
        cartItem.id = `cart-item-${item.courseId}`;

        // Set item content
        clone.querySelector('.course-img').src = item.thumbnailPath || '/img/course-placeholder.jpg';
        clone.querySelector('.course-img').alt = item.title;
        clone.querySelector('.course-title').textContent = item.title;

        // Additional course info if available
        if (item.author) {
            clone.querySelector('.course-author').textContent = `Giảng viên: ${item.author}`;
        } else {
            clone.querySelector('.course-author').textContent = 'Học Viện Online';
        }

        if (item.level) {
            clone.querySelector('.course-level').textContent = item.level;
        } else {
            clone.querySelector('.course-level').textContent = 'Tất cả cấp độ';
        }

        if (item.duration) {
            clone.querySelector('.course-duration').textContent = `${item.duration} giờ`;
        } else {
            clone.querySelector('.course-duration').textContent = '';
        }

        // Price display
        if (item.discountPrice && item.price > item.discountPrice) {
            clone.querySelector('.original-price').textContent = formatCurrency(item.price);
            clone.querySelector('.current-price').textContent = formatCurrency(item.discountPrice);
        } else {
            clone.querySelector('.original-price').textContent = '';
            clone.querySelector('.current-price').textContent = formatCurrency(item.price);
        }

        // Remove button event
        clone.querySelector('.remove-item').addEventListener('click', function() {
            removeCartItem(item.courseId);
        });

        // Add to DOM
        cartItemsList.appendChild(clone);
    });

    // Update total items count
    document.getElementById('totalItems').textContent = items.length;
}

/**
 * Update cart summary (subtotal, discount, total)
 * @param {Object} data - Cart data from API
 */
function updateCartSummary(data) {
    // Đảm bảo data và items tồn tại
    if (!data || !data.items) {
        console.error('Dữ liệu giỏ hàng không hợp lệ:', data);
        return;
    }

    const { items, total = 0, discount: apiDiscount } = data;

    // Calculate subtotal (price before discounts)
    let subtotal = 0;
    items.forEach(item => {
        // Sử dụng currentPrice hoặc discountPrice nếu có, nếu không thì dùng price
        const itemPrice = item.price || 0;
        subtotal += itemPrice * (item.quantity || 1);
    });

    // Sử dụng discount từ API nếu có, nếu không tính thủ công
    let discount = apiDiscount !== undefined ? apiDiscount : (subtotal - total);

    // Đảm bảo discount không âm
    discount = Math.max(0, discount);

    // Update DOM
    document.getElementById('subtotal').textContent = formatCurrency(subtotal);
    document.getElementById('discount').textContent = `-${formatCurrency(discount)}`;
    document.getElementById('total').textContent = formatCurrency(total);

    // Enable/disable checkout button
    document.getElementById('checkoutBtn').disabled = items.length === 0;
}

/**
 * Remove item from cart
 * @param {number} courseId - ID of course to remove
 */
function removeCartItem(courseId) {
    fetchWithAuth(`/api/cart/remove/${courseId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể xóa khóa học khỏi giỏ hàng');
            }
            return response.json();
        })
        .then(response => {
            if (response.success) {
                // Remove item from DOM
                const cartItem = document.getElementById(`cart-item-${courseId}`);
                if (cartItem) {
                    cartItem.remove();
                }
                updateCartBadge();
                // Update cart summary
                updateCartSummary(response);
                loadCartItems();

                // Update total items count
                const totalItems = parseInt(document.getElementById('totalItems').textContent) - 1;
                document.getElementById('totalItems').textContent = totalItems;

                // Show empty cart if no items left
                if (totalItems === 0) {
                    showEmptyCart();
                }

                showNotification('Đã xóa khóa học khỏi giỏ hàng', 'success');
            } else {
                showNotification(response.message || 'Có lỗi xảy ra khi xóa khỏi giỏ hàng', 'error');
            }
        })
        .catch(error => {
            console.error('Error removing item:', error);
            showNotification('Có lỗi xảy ra khi xóa khỏi giỏ hàng', 'error');
        });
}

/**
 * Clear entire cart
 */
function clearCart() {
    showConfirmation("Bạn có chắc chắn muốn xóa tất cả khóa học khỏi giỏ hàng?", function (){
        fetchWithAuth('/api/cart/clear', {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể xóa giỏ hàng');
                }
                return response.json();
            })
            .then(response => {
                if (response.success) {
                    // Update header cart badge
                    if (window.cart && window.cart.updateBadge) {
                        window.cart.updateBadge();
                    }

                    // Show empty cart
                    showEmptyCart();

                    showNotification('Đã xóa toàn bộ giỏ hàng', 'success');
                } else {
                    showNotification(response.message || 'Có lỗi xảy ra khi xóa giỏ hàng', 'error');
                }
            })
            .catch(error => {
                console.error('Error clearing cart:', error);
                showNotification('Có lỗi xảy ra khi xóa giỏ hàng', 'error');
            });
    }, function () {
        // Không làm gì cả
    });
}

/**
 * Apply promotion code
 */
function applyPromoCode() {
    const promoCode = document.getElementById('promoCode').value.trim();
    const promoMessage = document.getElementById('promoMessage');

    if (!promoCode) {
        promoMessage.textContent = 'Vui lòng nhập mã khuyến mãi';
        promoMessage.className = 'mt-2 small invalid';
        return;
    }

    fetchWithAuth('/api/cart/promo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ code: promoCode })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể áp dụng mã giảm giá');
            }
            return response.json();
        })
        .then(response => {
            if (response.success) {
                // Update promo message
                promoMessage.textContent = response.message || 'Đã áp dụng mã khuyến mãi';
                promoMessage.className = 'mt-2 small valid';

                // Update cart summary
                updateCartSummary(response);

                showNotification('Đã áp dụng mã giảm giá', 'success');
            } else {
                // Show error
                promoMessage.textContent = response.message || 'Mã khuyến mãi không hợp lệ';
                promoMessage.className = 'mt-2 small invalid';

                showNotification(response.message || 'Mã khuyến mãi không hợp lệ', 'error');
            }
        })
        .catch(error => {
            console.error('Error applying promo code:', error);
            promoMessage.textContent = 'Có lỗi xảy ra khi áp dụng mã';
            promoMessage.className = 'mt-2 small invalid';

            showNotification('Có lỗi xảy ra khi áp dụng mã khuyến mãi', 'error');
        });
}

/**
 * Proceed to checkout
 */
/**
 * Proceed to checkout
 */
function proceedToCheckout() {
    // First, get the cart data to ensure we have the latest course IDs
    fetchWithAuth('/api/cart')
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải giỏ hàng');
            }
            return response.json();
        })
        .then(response => {
            const { items } = response;

            // Check if cart is empty
            if (!items || items.length === 0) {
                showNotification('Không có khóa học nào trong giỏ hàng', 'warning');
                return;
            }

            // Extract course IDs from cart items
            const courseIds = items.map(item => item.courseId);

            // Proceed with checkout
            fetchWithAuth('/api/enrollments/checkout-multiple', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ courseIds: courseIds })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        showNotification('Thanh Toán Thành Công', 'success');
                        fetchWithAuth('/api/cart/clear', {
                            method: 'DELETE'
                        })
                            .then(response => {
                                if (!response.ok) {
                                    throw new Error('Không thể xóa giỏ hàng');
                                }
                                return response.json();
                            })
                            .then(response => {
                                if (response.success) {
                                    // Update header cart badge
                                    if (window.cart && window.cart.updateBadge) {
                                        window.cart.updateBadge();
                                    }

                                    // Show empty cart
                                    showEmptyCart();
                                }
                            })
                    } else {
                        showNotification(data.message || 'Có lỗi xảy ra khi thanh toán khóa học', 'error');
                    }
                });
        })
        .catch(error => {
            console.error('Error during checkout:', error);
            showNotification('Có lỗi xảy ra khi thanh toán khóa học', 'error');
        });
}

// Đảm bảo có thể gọi từ bên ngoài
window.updateCartTotal = function() {
    loadCartItems();
};