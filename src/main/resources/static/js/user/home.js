document.addEventListener('DOMContentLoaded', function () {
    const authenticatedPage = document.getElementById('authenticated');
    const unauthenticatedPage = document.getElementById('not-authenticated');
    authenticatedPage.style.display = "none";
    unauthenticatedPage.style.display = "none";
    // Fetch user data and update UI
    fetchWithAuth('/api/verify-token')
        .then(response => {
            if (!response.ok) {
                localStorage.clear();
                unauthenticatedPage.style.display = "block";
                //return Promise.reject('Authentication failed');
            }
            else if (response.ok){
                authenticatedPage.style.display = "block";
            }
            return response.json();
        })
        .then(data => {
            localStorage.setItem('username', data.username);
            localStorage.setItem('isAuthenticated', 'true');

            // Update username in the welcome section
            const usernameElement = document.getElementById('username');
            if (usernameElement && data.username) {
                usernameElement.textContent = data.username;
            }

            // Load user dashboard data
            return fetchWithAuth('/api/user/dashboard');
        })
        .then(response => {
            if (!response.ok) return Promise.reject('Failed to load dashboard data');
            return response.json();
        })
        .then(dashboard => {
            // Update dashboard with real data
            console.log('Dashboard data loaded:', dashboard);
            // TODO: Update UI with actual data
        })
        .catch(error => {
            console.error('Error:', error);
        });

    // Animation for learning path
    const pathItems = document.querySelectorAll('.path-item');
    pathItems.forEach((item, index) => {
        setTimeout(() => {
            item.classList.add('fade-in');
        }, index * 200);
    });
});
