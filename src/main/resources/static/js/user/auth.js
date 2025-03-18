document.addEventListener("DOMContentLoaded", function () {
    // DOM Elements
    const authContainer = document.querySelector(".auth-container");
    const showRegisterBtn = document.getElementById("showRegisterBtn");
    const showLoginBtn = document.getElementById("showLoginBtn");
    const mobileShowRegisterBtn = document.getElementById("mobileShowRegisterBtn");
    const mobileShowLoginBtn = document.getElementById("mobileShowLoginBtn");

    const loginForm = document.getElementById("loginForm");
    const registrationForm = document.getElementById("registrationForm");

    const loginPassword = document.getElementById("loginPassword");
    const registerPassword = document.getElementById("registerPassword");
    const confirmPassword = document.getElementById("confirmPassword");

    const togglePasswordBtns = document.querySelectorAll(".toggle-password");

    // Form switching functionality
    showRegisterBtn.addEventListener("click", function () {
        authContainer.classList.add("show-register");
    });

    showLoginBtn.addEventListener("click", function () {
        authContainer.classList.remove("show-register");
    });

    // Mobile version form switching
    if (mobileShowRegisterBtn) {
        mobileShowRegisterBtn.addEventListener("click", function (e) {
            e.preventDefault();
            authContainer.classList.add("show-register");
        });
    }

    if (mobileShowLoginBtn) {
        mobileShowLoginBtn.addEventListener("click", function (e) {
            e.preventDefault();
            authContainer.classList.remove("show-register");
        });
    }

    // Password visibility toggle
    togglePasswordBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
            const input = this.previousElementSibling;
            const icon = this.querySelector("i");

            if (input.type === "password") {
                input.type = "text";
                icon.classList.remove("fa-eye");
                icon.classList.add("fa-eye-slash");
            } else {
                input.type = "password";
                icon.classList.remove("fa-eye-slash");
                icon.classList.add("fa-eye");
            }
        });
    });

    // Add feedback elements if they don't exist
    function addFeedbackElement(inputId, message) {
        const input = document.getElementById(inputId);
        if (!input) return;

        // Check if feedback element already exists
        let feedbackId = inputId + "-feedback";
        let feedbackEl = document.getElementById(feedbackId);

        if (!feedbackEl) {
            feedbackEl = document.createElement("div");
            feedbackEl.id = feedbackId;
            feedbackEl.className = "invalid-feedback";
            feedbackEl.textContent = message;
            feedbackEl.style.display = "none";

            // Insert after the input group
            const inputGroup = input.closest('.input-group');
            inputGroup.parentNode.insertBefore(feedbackEl, inputGroup.nextSibling);
        }
        return feedbackEl;
    }

    // Setup feedback elements
    addFeedbackElement("loginUsername", "Please enter your username or email");
    addFeedbackElement("loginPassword", "Please enter your password");
    addFeedbackElement("registerUsername", "Username is required");
    addFeedbackElement("registerEmail", "Please enter a valid email address");
    const errorPassword = addFeedbackElement("registerPassword", "Password does not meet requirements");
    addFeedbackElement("confirmPassword", "Please confirm your password");

    // Password strength indicators for registration
    const lengthIndicator = document.getElementById("length");
    const capitalIndicator = document.getElementById("capital");
    const numberIndicator = document.getElementById("number");
    const specialIndicator = document.getElementById("special");

    registerPassword.addEventListener("input", function () {
        const value = this.value;
        let isValid = true;

        // Check length
        if (value.length >= 8) {
            lengthIndicator.classList.add("valid");
            lengthIndicator.style.display = "none";
        } else {
            lengthIndicator.classList.remove("valid");
            lengthIndicator.style.display = "block";
            isValid = false;
        }

        // Check uppercase
        if (/[A-Z]/.test(value)) {
            capitalIndicator.classList.add("valid");
            capitalIndicator.style.display = "none";
        } else {
            capitalIndicator.classList.remove("valid");
            capitalIndicator.style.display = "block";
            isValid = false;
        }

        // Check number
        if (/[0-9]/.test(value)) {
            numberIndicator.classList.add("valid");
            numberIndicator.style.display = "none";
        } else {
            numberIndicator.classList.remove("valid");
            numberIndicator.style.display = "block";
            isValid = false;
        }

        // Check special character
        if (/[^A-Za-z0-9]/.test(value)) {
            specialIndicator.classList.add("valid");
            specialIndicator.style.display = "none";
        } else {
            specialIndicator.classList.remove("valid");
            specialIndicator.style.display = "block";
            isValid = false;
        }

        // Update error message display based on overall validation
        if (errorPassword) {
            errorPassword.style.display = isValid ? "none" : "block";
        }
    });

    // Confirm password validation
    confirmPassword.addEventListener("input", function () {
        const feedbackEl = document.getElementById("confirmPassword-feedback");
        if (this.value !== registerPassword.value) {
            this.classList.add("is-invalid");
            if (feedbackEl) feedbackEl.style.display = "block";
        } else {
            this.classList.remove("is-invalid");
            if (feedbackEl) feedbackEl.style.display = "none";
        }
    });

    // Google login/register handling
    const googleBtns = document.querySelectorAll(".btn-google");
    googleBtns.forEach((btn) => {
        btn.addEventListener("click", function () {
            // In a real application, this would initiate Google OAuth flow
            console.log("Initiating Google authentication...");

            // Simulate API call delay
            setTimeout(() => {
                showSuccessMessage("Google Authentication", "Authentication successful! You will be redirected shortly.");
            }, 1000);
        });
    });

    // Login form submission
    loginForm.addEventListener("submit", function (e) {
        e.preventDefault();
        const usernameInput = document.getElementById('loginUsername').value;
        const isEmail = usernameInput.includes('@');
        const password = document.getElementById('loginPassword').value;
        let isError = false;

        if (!usernameInput) {
            document.getElementById('loginUsername-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('loginUsername-feedback').style.display = 'none';
        }

        if (!password) {
            document.getElementById('loginPassword-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('loginPassword-feedback').style.display = 'none';
        }

        if (isError) {
            return;
        }

        const loginData = {
            username: usernameInput,
            password: password,
            rememberMe: document.getElementById("rememberMe").checked
        };

        console.log("Login data:", loginData);

        // In a real application, you would send this data to your server
        // fetch('/api/login', {
        //     method: 'POST',
        //     headers: {
        //         'Content-Type': 'application/json'
        //     },
        //     body: JSON.stringify(loginData)
        // })
        // .then(response => {
        //     if (response.ok) {
        //         showSuccessMessage("Login Successful", "You are now being redirected to the dashboard.");
        //     } else {
        //         showPopupNotification("Login failed. Please check your credentials.");
        //     }
        // });

        // For demo purposes, show success message
        showSuccessMessage("Login Successful", "You are now being redirected to the dashboard.");
    });

    // Registration form submission
    registrationForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const username = document.getElementById('registerUsername').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;
        const confirmPasswordValue = document.getElementById('confirmPassword').value;
        let isError = false;

        if (!username) {
            document.getElementById('registerUsername-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('registerUsername-feedback').style.display = 'none';
        }

        if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            document.getElementById('registerEmail-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('registerEmail-feedback').style.display = 'none';
        }

        if (!password || password.length < 8 ||
            !/[A-Z]/.test(password) ||
            !/[0-9]/.test(password) ||
            !/[^A-Za-z0-9]/.test(password)) {
            document.getElementById('registerPassword-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('registerPassword-feedback').style.display = 'none';
        }

        if (!confirmPasswordValue || confirmPasswordValue !== password) {
            document.getElementById('confirmPassword-feedback').style.display = 'block';
            isError = true;
        } else {
            document.getElementById('confirmPassword-feedback').style.display = 'none';
        }

        if (!document.getElementById('terms').checked) {
            isError = true;
        }

        if (isError) {
            return;
        }

        const accountData = {
            username: username,
            email: email,
            password: password
        };

        console.log("Registration data:", accountData);

        // In a real application, you would send this data to your server
        fetch('/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(accountData)
        })
        .then(response => {
            if (response.ok) {
                showPopupNotification("Registration successful! Please verify your email to login.");
                registrationForm.reset();
            } else if (response.status === 409) {
                showPopupNotification('Email already exists');
            } else {
                showPopupNotification('Registration failed. Please try again.');
            }
        });

        // For demo purposes, show success message
        showSuccessMessage("Registration Successful", "Your student account has been created successfully.");
        registrationForm.reset();

        // Reset validation indicators
        lengthIndicator.classList.remove("valid");
        capitalIndicator.classList.remove("valid");
        numberIndicator.classList.remove("valid");
        specialIndicator.classList.remove("valid");
    });

    // Success message display
    function showSuccessMessage(title, message) {
        const successModal = new bootstrap.Modal(document.getElementById("successModal"));
        document.getElementById("successModalTitle").textContent = title;
        document.getElementById("successModalMessage").textContent = message;
        successModal.show();

        // Simulate redirection after login/register
        setTimeout(() => {
            // In a real application, redirect to dashboard or home page
            // window.location.href = 'dashboard.html';
            console.log("Redirecting to dashboard...");
        }, 2000);
    }

    // Notification popup display
    function showPopupNotification(message) {
        // Create notification element if it doesn't exist
        let notification = document.getElementById("popup-notification");
        if (!notification) {
            notification = document.createElement("div");
            notification.id = "popup-notification";
            notification.className = "popup-notification";
            document.body.appendChild(notification);

            // Add the style for the notification if not already in CSS
            const style = document.createElement("style");
            style.textContent = `
                .popup-notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    z-index: 9999;
                    background-color: #333;
                    color: white;
                    padding: 15px 25px;
                    border-radius: 5px;
                    box-shadow: 0 4px 8px rgba(0,0,0,0.2);
                    transform: translateY(-100px);
                    opacity: 0;
                    transition: all 0.3s ease;
                }
                .popup-notification.show {
                    transform: translateY(0);
                    opacity: 1;
                }
            `;
            document.head.appendChild(style);
        }

        // Set message and show notification
        notification.textContent = message;
        notification.classList.add("show");

        // Hide after 3 seconds
        setTimeout(() => {
            notification.classList.remove("show");
        }, 3000);
    }

    // Add slide animation effect on page load
    setTimeout(() => {
        document.querySelector(".auth-form").classList.add("animated");
    }, 100);
});