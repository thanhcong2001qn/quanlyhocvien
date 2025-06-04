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
    const notification = document.getElementById('successNotification');

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
    // Form submission handling - Login
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const usernameInput = document.getElementById('username').value;
            const passwordInput = document.getElementById('password').value;
            const submitBtn = this.querySelector('.auth-btn');

            // Add loading state
            submitBtn.classList.add('loading');
            const loginData = {
                username: usernameInput,
                password: passwordInput
            }
            fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(loginData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(errorMsg => {
                            if (errorMsg === "Invalid username or password") {
                                showNotification(
                                    'error',
                                    'Login Failed',
                                    'Invalid username or password.',
                                    5000
                                )
                            }
                            throw new Error(errorMsg);
                        });
                    }
                    return response.json();
                })
                .then(data => {
                        // Đăng nhập thành công, nhận dữ liệu người dùng từ response
                        showNotification(
                            'success',
                            'Login Successful!',
                            'You have successfully logged in.',
                            5000 // 5 seconds
                        );

                        // Lưu thông tin người dùng vào localStorage
                        localStorage.setItem('username', data.username);
                        localStorage.setItem('isAuthenticated', 'true');

                        // Lưu vai trò người dùng
                        localStorage.setItem('isAdmin', data.isAdmin);
                        localStorage.setItem('isTeacher', data.isTeacher);
                        localStorage.setItem('isStudent', data.isStudent);

                        // Nếu API trả về JWT token thì lưu token
                        if (data.token) {
                            localStorage.setItem('token', data.token);
                        }
                        // Chuyển hướng dựa trên vai trò
                        if (data.isAdmin === true) {
                            window.location.href = '/dashboard';
                        } else if (data.isStudent === true || data.isTeacher === true) {
                            window.location.href = '/home';
                        } else {
                            // Trường hợp mặc định nếu không có vai trò xác định
                            window.location.href = '/home';
                        }
                    }).catch(error => {
                        console.error('Login error:', error);
                        // Error already displayed in previous error handlers
                    }).finally(() => {
                        // Remove loading state
                        submitBtn.classList.remove('loading');
                    });
        });
    }
});