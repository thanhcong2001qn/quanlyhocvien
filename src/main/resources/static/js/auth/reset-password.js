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
function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    const icon = input.nextElementSibling.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
    }
}
document.addEventListener('DOMContentLoaded', function () {
    const resetPasswordForm = document.getElementById('resetPasswordForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    // Password strength meter
    const strengthProgress = document.getElementById('strength-progress');
    const passwordFeedback = document.getElementById('password-feedback');

    if (passwordInput) {
        passwordInput.addEventListener('input', function () {
            const password = this.value;
            let strength = 0;
            let feedback = '';

            // Calculate password strength
            if (password.length >= 8) {
                strength += 25;
            }

            if (password.match(/[A-Z]/)) {
                strength += 25;
            }

            if (password.match(/[0-9]/)) {
                strength += 25;
            }

            if (password.match(/[^A-Za-z0-9]/)) {
                strength += 25;
            }

            // Update progress bar
            strengthProgress.style.width = strength + '%';

            // Set color based on strength
            if (strength <= 25) {
                strengthProgress.className = 'progress-bar bg-danger';
                feedback = 'Weak: Please use a stronger password';
            } else if (strength <= 50) {
                strengthProgress.className = 'progress-bar bg-warning';
                feedback = 'Fair: Add uppercase letters, numbers or special characters';
            } else if (strength <= 75) {
                strengthProgress.className = 'progress-bar bg-info';
                feedback = 'Good: Add uppercase letters, numbers or special characters';
            } else {
                strengthProgress.className = 'progress-bar bg-success';
                feedback = 'Strong: Great password!';
            }

            passwordFeedback.textContent = feedback;

            // Check password match if confirm password has input
            if (confirmPasswordInput && confirmPasswordInput.value) {
                validatePasswordMatch();
            }
        });
    }


    // Form submission
    resetPasswordForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        const password = passwordInput.value;

        // Send reset password request
        fetch('/api/reset-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                token: token,
                password: password
            })
        })
            .then(response => {
                if (response.ok) {
                    showNotification('success',
                        'Reset Successful',
                        'Your password has been reset successfully!'
                    );
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 3000);
                } else {
                    return response.text().then(text => {
                        throw new Error(text || 'Failed to reset password');
                    });
                }
            })
            .catch(error => {
                showNotification('error', error.message);
            });
    });
});