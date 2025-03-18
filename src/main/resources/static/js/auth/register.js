// Toggle password visibility for registration page
function togglePasswordReg(inputId) {
    const passwordInput = document.getElementById(inputId);
    const icon = passwordInput.parentElement.querySelector('.password-toggle i');

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
document.addEventListener('DOMContentLoaded', function() {
    // Password strength meter
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
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
    function isPasswordValid(password) {
        return password.length >= 8 &&
            /[A-Z]/.test(password) &&
            /[0-9]/.test(password) &&
            /[^A-Za-z0-9]/.test(password);
    }
    // Validate password match
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', validatePasswordMatch);

        function validatePasswordMatch() {
            if (passwordInput.value === confirmPasswordInput.value) {
                confirmPasswordInput.classList.remove('is-invalid');
                confirmPasswordInput.classList.add('is-valid');

                // Remove any existing feedback element
                const existingFeedback = confirmPasswordInput.parentElement.nextElementSibling;
                if (existingFeedback && existingFeedback.classList.contains('invalid-feedback')) {
                    existingFeedback.remove();
                }
            } else {
                confirmPasswordInput.classList.remove('is-valid');
                confirmPasswordInput.classList.add('is-invalid');

                // Add feedback if it doesn't exist
                let feedback = confirmPasswordInput.parentElement.nextElementSibling;
                if (!feedback || !feedback.classList.contains('invalid-feedback')) {
                    feedback = document.createElement('div');
                    feedback.className = 'invalid-feedback';
                    feedback.textContent = 'Passwords do not match';
                    confirmPasswordInput.parentElement.after(feedback);
                }
            }
        }
    }
    // Form submission handling - Register
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();

            const submitBtn = this.querySelector('.auth-btn');

            // Validate form
            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            // Perform validation
            let isValid = true;

            if (username.length < 3) {
                isValid = false;
                showError('username', 'Username must be at least 3 characters');
            } else {
                clearError('username');
            }

            if (!validateEmail(email)) {
                isValid = false;
                showError('email', 'Please enter a valid email address');
            } else {
                clearError('email');
            }

            if (password !== confirmPassword) {
                isValid = false;
                showError('confirmPassword', 'Passwords do not match');
            } else {
                clearError('confirmPassword');
            }

            if (isValid) {
                // Add loading state
                submitBtn.classList.add('loading');

                // Simulate API call (replace with actual API call)
                const registerData = {
                    username: username,
                    email: email,
                    password: password
                }
                fetch('api/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(registerData)
                })
                    .then(response => {
                        if (response.ok) {
                            showNotification(
                                'success',
                                'Registration Successful!',
                                'Your account has been created successfully. Please check your email for verification.',
                                5000 // 5 seconds
                            );
                            // Clear form fields
                            registerForm.reset();
                        }
                        return response.text().then(errorMsg => {
                            if (errorMsg === "Username already exists") {
                                showNotification(
                                    'error',
                                    'Registration Failed',
                                    'Username is already taken. Please choose a different username.'
                                );
                            } else if (errorMsg === "Email already exists") {
                                showNotification(
                                    'error',
                                    'Registration Failed',
                                    'An account with that email already exists. Please use a different email address.'
                                );
                            } else if (response.status === 0) {
                                showNotification(
                                    'error',
                                    'Registration Failed',
                                    'An error occurred during registration. Please try again later.'
                                );
                            }
                        });
                    });
                setTimeout(function () {
                    submitBtn.classList.remove('loading');

                    // Success message (replace with your actual logic)

                    // In a real application, you would redirect the user
                    // window.location.href = 'login.html';
                }, 1500);
            }

        })

        // Email validation helper
        function validateEmail(email) {
            const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return re.test(String(email).toLowerCase());
        }

        // Show error message
        function showError(inputId, message) {
            const input = document.getElementById(inputId);
            input.classList.add('is-invalid');
            // Check if error message already exists
            let feedback = input.parentElement.nextElementSibling;
            if (!feedback || !feedback.classList.contains('invalid-feedback')) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                input.parentElement.after(feedback);
            }
            feedback.textContent = message;
        }

        // Clear error message
        function clearError(inputId) {
            const input = document.getElementById(inputId);
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');

            // Remove error message if it exists
            const feedback = input.parentElement.nextElementSibling;
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.remove();
            }
        }
    }
});