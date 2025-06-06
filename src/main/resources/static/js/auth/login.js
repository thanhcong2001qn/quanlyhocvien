// Toggle password visibility for login page
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const icon = document.querySelector('.password-toggle i');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
}
function showNotification(type, title, message, duration = 5000) {
    const notification = document.getElementById('errorNotification');

    // Update notification content
    notification.querySelector('h4').textContent = title;
    notification.querySelector('p').textContent = message;

    // Set notification type
    notification.className = 'notification ' + type;

    // Show notification
    notification.classList.add('show');

    // Reset progress animation
    const progressBar = notification.querySelector('.notification-progress');
    progressBar.style.animation = 'none';
    progressBar.offsetHeight; // Trigger reflow
    progressBar.style.animation = `progress ${duration/1000}s linear forwards`;

    // Auto hide after duration
    window.notificationTimeout = setTimeout(() => {
        closeNotification();
    }, duration);
}
function closeNotification() {
    const notification = document.getElementById('successNotification');
    notification.classList.remove('show');
    clearTimeout(window.notificationTimeout);
}
// Document ready
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        // Lấy thông báo lỗi từ session hoặc sử dụng thông báo mặc định
        const errorMessage = /*[[${session.SPRING_SECURITY_LAST_EXCEPTION_MESSAGE != null ? session.SPRING_SECURITY_LAST_EXCEPTION_MESSAGE : 'Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại.'}]]*/'Sai tên đăng nhập hoặc mật khẩu';
        showNotification('error', 'Lỗi đăng nhập', errorMessage, 5000);
    }
});