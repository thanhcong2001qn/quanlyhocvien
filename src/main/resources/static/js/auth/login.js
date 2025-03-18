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
                body: JSON.stringify(loginData)
            })
                .then(response => {
                    if (response.ok) {
                        showNotification(
                            'success',
                            'Login Successful!',
                            'Your account has been created successfully. Please check your email for verification.',
                            5000 // 5 seconds
                        );
                        window.location.href = '/user'
                    }
                    return response.text().then(errorMsg => {
                        if (errorMsg === "Invalid username or password") {
                            showNotification(
                                'error',
                                'Login Failed',
                                'Invalid username or password.',
                                5000
                            )
                        } else if (errorMsg === "Email is not verified"){
                            showNotification(
                                'error',
                                'Login Failed',
                                'Email is not verified.',
                                5000
                            )
                        }
                    });
                });
            // Simulate API call (replace with actual API call)
            setTimeout(function() {
                submitBtn.classList.remove('loading');

                // Success message (replace with your actual logic)
                // alert('Login successful!');

                // In a real application, you would redirect the user
                // window.location.href = 'dashboard.html';
            }, 1500);
        });
    }
});