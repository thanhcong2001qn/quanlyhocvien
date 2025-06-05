function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
        ...options.headers || {},
//        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    return fetch(url, {
        ...options,
        headers: headers
    });
}

const api = {
    /**
     * Send API request with authentication
     * @param {string} url - API endpoint
     * @param {string} method - HTTP method
     * @param {object} data - Request data
     * @param {boolean} addContentType - Whether to add JSON content type header
     * @returns {Promise} - Promise with response
     */
    request: function(url, method = 'GET', data = null, addContentType = true) {
        // Setup request options
        const options = {
            method: method,
            headers: {}
        };

        // Add Content-Type header if required (không thêm cho FormData)
        if (addContentType && !(data instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
        }

        // Add request body for POST/PUT requests
        if (data) {
            if (method === 'GET') {
                // For GET requests, add parameters to URL
                const params = new URLSearchParams();
                Object.keys(data).forEach(key => {
                    params.append(key, data[key]);
                });
                url = `${url}?${params.toString()}`;
            } else if (data instanceof FormData) {
                // If FormData, use it directly
                options.body = data;
            } else {
                // Otherwise, stringify as JSON
                options.body = JSON.stringify(data);
            }
        }

        // Send request using fetchWithAuth
        return fetchWithAuth(url, options)
            .then(response => {
                if (!response.ok) {
                    // Log error details for debugging
                    console.error('API Error:', response.status, response.statusText);
                    console.error('URL:', url);
                    console.error('Method:', method);

                    // Check if token expired (401 Unauthorized)
                    if (response.status === 401) {
                        this.handleAuthError();
                    }

                    // Try to parse error response
                    return response.json()
                        .catch(() => {
                            // If can't parse as JSON, return text
                            return response.text().then(text => {
                                return { message: text || `Error ${response.status}: ${response.statusText}` };
                            });
                        })
                        .then(errData => {
                            throw errData;
                        });
                }

                // Check if response is empty
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                }

                return response.text().then(text => {
                    // If empty response
                    if (!text) return {};

                    // Try to parse as JSON
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        return { message: text };
                    }
                });
            });
    },

    /**
     * Handle authentication errors
     */
    handleAuthError: function() {
        // Clear token
        localStorage.removeItem('token');

        // Show message
        if (typeof showNotification === 'function') {
            showNotification('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'error');
        }

        // Redirect after a short delay
        setTimeout(() => {
            window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`;
        }, 1000);
    },

    /**
     * GET request
     * @param {string} url - API endpoint
     * @param {object} params - Query parameters
     * @returns {Promise} - Promise with response
     */
    get: function(url, params = null) {
        return this.request(url, 'GET', params);
    },

    /**
     * POST request
     * @param {string} url - API endpoint
     * @param {object} data - Request data
     * @param {boolean} addContentType - Whether to add JSON content type header
     * @returns {Promise} - Promise with response
     */
    post: function(url, data, addContentType = true) {
        return this.request(url, 'POST', data, addContentType);
    },

    /**
     * PUT request
     * @param {string} url - API endpoint
     * @param {object} data - Request data
     * @param {boolean} addContentType - Whether to add JSON content type header
     * @returns {Promise} - Promise with response
     */
    put: function(url, data, addContentType = true) {
        return this.request(url, 'PUT', data, addContentType);
    },

    /**
     * DELETE request
     * @param {string} url - API endpoint
     * @returns {Promise} - Promise with response
     */
    delete: function(url) {
        return this.request(url, 'DELETE');
    },

    /**
     * Upload file(s)
     * @param {string} url - API endpoint
     * @param {FormData} formData - Form data with files
     * @returns {Promise} - Promise with response
     */
    upload: function(url, formData) {
        return this.request(url, 'POST', formData, false);
    }
};

// Export functions to global scope
window.fetchWithAuth = fetchWithAuth;
window.api = api;
function navigateWithAuth(url) {
    const token = localStorage.getItem('token');

    // If using fetch API for navigation
    fetch(url, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else {
                window.location.href = url;
            }
        })
        .catch(error => console.error('Navigation error:', error));
}