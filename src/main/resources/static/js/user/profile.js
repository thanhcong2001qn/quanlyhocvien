document.addEventListener('DOMContentLoaded', function() {
    // Verify token and load profile data
    fetchWithAuth('/api/verify-token')
        .then(response => {
            if (!response.ok) {
                localStorage.clear();
                window.location.href = '/login';
                return Promise.reject('Unauthorized');
            }
            return fetchWithAuth('/api/user/profile');
        })
        .then(response => response.json())
        .then(profileData => {
            populateProfileData(profileData);
            loadEnrolledCourses();
            loadLearningHistory();
        })
        .catch(error => {
            console.error('Error loading profile:', error);
        });

    // Avatar change functionality
    document.getElementById('change-avatar-btn').addEventListener('click', function() {
        document.getElementById('avatar-upload').click();
    });

    document.getElementById('avatar-upload').addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById('profile-image').src = e.target.result;
                uploadAvatar(file);
            };
            reader.readAsDataURL(file);
        }
    });

    // Form submissions
    document.getElementById('personal-info-form').addEventListener('submit', function(e) {
        e.preventDefault();
        updatePersonalInfo();
    });

    document.getElementById('change-password-form').addEventListener('submit', function(e) {
        e.preventDefault();
        changePassword();
    });
});

function populateProfileData(data) {
    // Update profile header
    document.getElementById('student-name').textContent = data.fullName;
    document.getElementById('student-id').innerHTML = `<i class="fas fa-id-card me-2"></i>Mã học viên: ${data.studentId}`;
    document.getElementById('student-email').innerHTML = `<i class="fas fa-envelope me-2"></i>${data.email}`;
    document.getElementById('student-phone').innerHTML = `<i class="fas fa-phone me-2"></i>${data.phone || 'Chưa cập nhật'}`;
    document.getElementById('join-date').innerHTML = `<i class="fas fa-calendar-alt me-2"></i>Ngày tham gia: ${formatDate(data.joinDate)}`;

    if (data.profileImage) {
        document.getElementById('profile-image').src = data.profileImage;
    }

    // Update form fields
    document.getElementById('full-name').value = data.fullName;
    document.getElementById('date-of-birth').value = data.dateOfBirth || '';
    document.getElementById('email').value = data.email;
    document.getElementById('phone').value = data.phone || '';
    document.getElementById('address').value = data.address || '';
    if (data.isEmailVerified === false) {
        document.getElementById('email-not-verified').value = 'Email chưa được xác thực';
        document.getElementById('email-not-verified').style.display = 'block';
    }
}

function loadEnrolledCourses() {
    fetchWithAuth('/api/student/enrolled-courses')
        .then(response => response.json())
        .then(courses => {
            const container = document.getElementById('enrolled-courses-container');
            container.innerHTML = '';

            if (courses.length === 0) {
                container.innerHTML = '<div class="col-12 text-center py-5">Bạn chưa đăng ký khóa học nào.</div>';
                return;
            }

            courses.forEach(course => {
                container.innerHTML += `
            <div class="col-md-6 col-lg-4">
              <div class="card course-card">
                <img src="${course.thumbnailUrl || 'https://via.placeholder.com/300x150'}" class="card-img-top" alt="${course.title}">
                <div class="card-body">
                  <h5 class="card-title">${course.title}</h5>
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <small class="text-muted">${course.category}</small>
                    <span class="badge bg-${getStatusBadgeColor(course.status)}">${course.status}</span>
                  </div>
                  <div class="mb-2">
                    <small class="text-muted">Tiến độ:</small>
                    <div class="progress mt-1">
                      <div class="progress-bar" role="progressbar" style="width: ${course.progressPercentage}%;"
                           aria-valuenow="${course.progressPercentage}" aria-valuemin="0" aria-valuemax="100">
                        ${course.progressPercentage}%
                      </div>
                    </div>
                  </div>
                  <a href="/course/${course.id}" class="btn btn-primary btn-sm">Tiếp tục học</a>
                </div>
              </div>
            </div>
          `;
            });
        })
        .catch(error => {
            console.error('Error loading enrolled courses:', error);
            document.getElementById('enrolled-courses-container').innerHTML =
                '<div class="col-12 text-center">Đã xảy ra lỗi khi tải khóa học. Vui lòng thử lại sau.</div>';
        });
}

function loadLearningHistory() {
    fetchWithAuth('/api/student/learning-history')
        .then(response => response.json())
        .then(history => {
            const tableBody = document.getElementById('learning-history');
            tableBody.innerHTML = '';

            if (history.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="4" class="text-center">Chưa có lịch sử học tập.</td></tr>';
                return;
            }

            history.forEach(item => {
                tableBody.innerHTML += `
            <tr>
              <td>${item.courseName}</td>
              <td>${item.grade || 'N/A'}</td>
              <td>
                <div class="progress">
                  <div class="progress-bar" role="progressbar" style="width: ${item.progress}%;"
                       aria-valuenow="${item.progress}" aria-valuemin="0" aria-valuemax="100">
                    ${item.progress}%
                  </div>
                </div>
              </td>
              <td><span class="badge bg-${getStatusBadgeColor(item.status)}">${item.status}</span></td>
            </tr>
          `;
            });
        })
        .catch(error => {
            console.error('Error loading learning history:', error);
            document.getElementById('learning-history').innerHTML =
                '<tr><td colspan="4" class="text-center">Đã xảy ra lỗi khi tải lịch sử học tập. Vui lòng thử lại sau.</td></tr>';
        });
}

function updatePersonalInfo() {
    const personalInfo = {
        fullName: document.getElementById('full-name').value,
        dateOfBirth: document.getElementById('date-of-birth').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value
    };

    fetchWithAuth('/api/student/update-profile', {
        method: 'POST',
        body: JSON.stringify(personalInfo)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to update profile');
            }
            return response.json();
        })
        .then(data => {
            alert('Cập nhật thông tin cá nhân thành công!');
            populateProfileData(data);
        })
        .catch(error => {
            console.error('Error updating profile:', error);
            alert('Đã xảy ra lỗi khi cập nhật thông tin. Vui lòng thử lại sau.');
        });
}

function changePassword() {
    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (newPassword !== confirmPassword) {
        alert('Mật khẩu mới và xác nhận mật khẩu không khớp!');
        return;
    }

    fetchWithAuth('/api/student/change-password', {
        method: 'POST',
        body: JSON.stringify({
            currentPassword,
            newPassword
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to change password');
            }
            return response.json();
        })
        .then(() => {
            alert('Đổi mật khẩu thành công!');
            document.getElementById('change-password-form').reset();
        })
        .catch(error => {
            console.error('Error changing password:', error);
            alert('Đã xảy ra lỗi khi đổi mật khẩu. Vui lòng kiểm tra lại mật khẩu hiện tại.');
        });
}

function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('avatar', file);

    fetchWithAuth('/api/student/upload-avatar', {
        method: 'POST',
        body: formData,
        headers: {} // Let the browser set the content type for FormData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to upload avatar');
            }
            return response.json();
        })
        .then(data => {
            alert('Cập nhật ảnh đại diện thành công!');
        })
        .catch(error => {
            console.error('Error uploading avatar:', error);
            alert('Đã xảy ra lỗi khi tải lên ảnh đại diện. Vui lòng thử lại sau.');
        });
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function getStatusBadgeColor(status) {
    switch(status.toLowerCase()) {
        case 'hoàn thành':
        case 'completed':
            return 'success';
        case 'đang học':
        case 'in progress':
            return 'primary';
        case 'chưa bắt đầu':
        case 'not started':
            return 'secondary';
        default:
            return 'info';
    }
}
function resendEmail(){
    const email = document.getElementById('email').value;
    const emailData = {
        email: email
    };
    fetch('/api/resend-verification-email', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(
            emailData
        )
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMsg => {
                    if (errorMsg === "Token is still valid") {
                        showNotification('Email đã được gửi, vui lòng kiểm tra hộp thư đến hoặc spam');
                    }
                    throw new Error(errorMsg);
                });
            }
            return response.json();
        })
        .then(() => {
            showNotification('Đã gửi email xác thực!');
        }).catch(error => {
            console.error('Login error:', error);
            // Error already displayed in previous error handlers
        })
}
