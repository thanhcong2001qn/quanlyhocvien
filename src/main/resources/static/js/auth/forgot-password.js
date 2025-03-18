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
document.addEventListener('DOMContentLoaded', function () {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');

    forgotPasswordForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const emailOrUsername = document.getElementById('emailOrUsername').value.trim();

        if (!emailOrUsername) {
            showNotification('error', 'Please enter your email or username');
            return;
        }

        // Send reset password request
        fetch('/api/forgot-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({emailOrUsername: emailOrUsername})
        })
            .then(response => {
                if (response.ok) {
                    showNotification('success',
                        'Reset Link Sent',
                        'Password reset link has been sent to your email'
                    );
                    forgotPasswordForm.reset();
                } else {
                    return response.text().then(text => {
                        showNotification(
                            'error',
                            'Reset Link Failed',
                            'Account not found'
                        )
                    });
                }
            })
            .catch(error => {
                showNotification('error', error.message);
            });
    });
});