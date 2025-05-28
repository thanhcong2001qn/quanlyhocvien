/**
 * Course Detail Page JavaScript
 * Handles modules and lessons management
 *
 * @author thanhcong2001qn
 * @version 1.0
 * @date 2025-05-27 13:23:41
 */

// Global variables
let courseId = null;
let courseData = null;
let modules = [];
let currentModuleId = null;
let currentLessonId = null;
let deleteType = null;
let deleteId = null;

// Bootstrap modals
let moduleModal = null;
let lessonModal = null;
let deleteConfirmModal = null;

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the page
    initPage();

    // Setup event listeners
    setupEventListeners();

    // Initialize Bootstrap modals
    initModals();
});

/**
 * Initialize the page
 */
function initPage() {
    // Get course ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    courseId = urlParams.get('id');

    if (!courseId) {
        const pathParts = window.location.pathname.split('/');
        if (pathParts.length > 2 && pathParts[1] === 'courseDetail') {
            courseId = pathParts[2];
        }
    }

    if (!courseId) {
        showErrorMessage('Không tìm thấy ID khóa học');
        return;
    }

    // Load course data
    loadCourseData();
}

/**
 * Initialize Bootstrap modals
 */
function initModals() {
    moduleModal = new bootstrap.Modal(document.getElementById('moduleModal'));
    lessonModal = new bootstrap.Modal(document.getElementById('lessonModal'));
    deleteConfirmModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Add Module button
    document.getElementById('addModuleBtn').addEventListener('click', openAddModuleModal);

    // Save Module button
    document.getElementById('saveModuleBtn').addEventListener('click', saveModule);

    // Save Lesson button
    document.getElementById('saveLessonBtn').addEventListener('click', saveLesson);

    // Confirm Delete button
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);

    // Lesson Type change
    document.getElementById('lessonType').addEventListener('change', handleLessonTypeChange);

    // Edit Course button
    document.getElementById('editCourseBtn').addEventListener('click', function() {
        window.location.href = `/edit-course/${courseId}`;
    });

    // Edit Description button
    document.getElementById('editDescriptionBtn').addEventListener('click', function() {
        window.location.href = `/edit-course/${courseId}?tab=description`;
    });

    // Preview Course button
    document.getElementById('previewCourseBtn').addEventListener('click', function() {
        window.open(`/courses/${courseId}`, '_blank');
    });

    // Publish Course button
    document.getElementById('publishCourseBtn').addEventListener('click', function() {
        updateCourseStatus(true);
    });

    // Unpublish Course button
    document.getElementById('unpublishCourseBtn').addEventListener('click', function() {
        updateCourseStatus(false);
    });

    // Toggle Featured button
    document.getElementById('toggleFeaturedBtn').addEventListener('click', toggleFeatured);

    // Module tab clicked - ensure module list is loaded
    document.getElementById('modules-tab').addEventListener('click', function() {
        if (modules.length === 0) {
            loadModules();
        }
    });
}

/**
 * Load course data from API
 */
function loadCourseData() {
    showLoading(true);

    fetchWithAuth(`/api/courses/${courseId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải thông tin khóa học');
            }
            return response.json();
        })
        .then(data => {
            courseData = data;
            populateCourseData(data);
            showLoading(false);
        })
        .catch(error => {
            console.error('Error loading course data:', error);
            showErrorMessage('Không thể tải thông tin khóa học');
            showLoading(false);
        });
}

/**
 * Populate course data into the UI
 */
function populateCourseData(course) {
    // Basic info
    document.getElementById('courseTitle').textContent = course.title;
    document.title = `${course.title} - Chi Tiết Khóa Học`;

    // Thumbnail
    if (course.thumbnailPath) {
        document.getElementById('courseThumbnail').src = course.thumbnailPath;
    }

    // Course details
    document.getElementById('courseLevel').textContent = course.level || 'Chưa xác định';
    document.getElementById('courseCategory').textContent = course.category ? course.category.categoryName : 'Chưa phân loại';

    // Status
    const statusBadge = document.getElementById('courseStatus');
    if (course.isPublished) {
        statusBadge.className = 'badge bg-success';
        statusBadge.textContent = 'Đã xuất bản';
    } else {
        statusBadge.className = 'badge bg-secondary';
        statusBadge.textContent = 'Chưa xuất bản';
    }

    // Featured status
    if (course.isFeatured) {
        document.getElementById('toggleFeaturedBtn').textContent = 'Bỏ đánh dấu nổi bật';
    } else {
        document.getElementById('toggleFeaturedBtn').textContent = 'Đánh dấu nổi bật';
    }

    // Course duration and counts
    document.getElementById('courseDuration').textContent = formatDuration(course.duration || 0);
    document.getElementById('modulesCount').textContent = `${course.moduleCount || 0} modules`;
    document.getElementById('lessonsCount').textContent = `${course.lessonCount || 0} bài học`;
    document.getElementById('enrollmentsCount').textContent = `${course.enrollmentCount || 0} học viên`;

    // Price
    document.getElementById('coursePrice').textContent = formatCurrency(course.price || 0);
    if (course.discountPrice && course.discountPrice < course.price) {
        document.getElementById('courseOriginalPrice').textContent = formatCurrency(course.price);
        document.getElementById('coursePrice').textContent = formatCurrency(course.discountPrice);
    } else {
        document.getElementById('courseOriginalPrice').textContent = '';
    }

    // Dates
    document.getElementById('courseCreatedAt').textContent = formatDate(course.createdAt);
    document.getElementById('courseUpdatedAt').textContent = formatDate(course.updatedAt);
    document.getElementById('coursePublishedAt').textContent = course.publishedAt ? formatDate(course.publishedAt) : 'Chưa xuất bản';

    // Description
    if (course.description) {
        document.getElementById('courseDescription').innerHTML = course.description;
    }

    // Update module status
    updateModuleStatus(course.moduleCount > 0);

    // Update completion percentage
    updateCompletionPercentage();
}

/**
 * Load modules for the course
 */
function loadModules() {
    fetchWithAuth(`/api/modules/${courseId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải danh sách module');
            }
            return response.json();
        })
        .then(data => {
            modules = data;
            return loadLessonsForAllModules(modules);
        })
        .then(modulesWithLessons => {
            renderModules(modulesWithLessons);
            initSortable();
            return modulesWithLessons;
        })
        .catch(error => {
            console.error('Error loading modules:', error);
            showErrorMessage('Không thể tải danh sách module');
        });
}

/**
 * Render modules and their lessons
 */
function renderModules(modules) {
    const moduleList = document.getElementById('moduleList');
    const noModulesMessage = document.getElementById('noModulesMessage');

    if (!modules || modules.length === 0) {
        noModulesMessage.style.display = 'block';
        moduleList.innerHTML = '';
        return;
    }
    if (noModulesMessage !== null) {
        noModulesMessage.style.display = 'none';
    }
    let moduleHtml = '';
    modules.forEach(module => {
        moduleHtml += `
            <div class="module-card" data-module-id="${module.moduleId}">
                <div class="module-header">
                    <h5>
                        <i class="fas fa-grip-vertical me-2 text-muted"></i>
                        <span class="module-order">${module.position || 1}.</span> 
                        ${module.title}
                    </h5>
                    <div class="module-actions">
                        <button class="btn btn-sm btn-outline-primary edit-module-btn" 
                                data-module-id="${module.moduleId}" title="Chỉnh sửa module">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger delete-module-btn" 
                                data-module-id="${module.moduleId}" title="Xóa module">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>
                </div>
                <div class="module-content">
                    <div class="lesson-list-container">
                        ${renderLessons(module.lessons || [], module.moduleId)}
                    </div>
                    <div class="add-lesson-container">
                        <button class="btn btn-outline-success add-lesson-btn" data-module-id="${module.moduleId}">
                            <i class="fas fa-plus"></i> Thêm bài học
                        </button>
                    </div>
                </div>
            </div>
        `;
    });

    moduleList.innerHTML = moduleHtml;

    // Add event listeners to the buttons
    document.querySelectorAll('.edit-module-btn').forEach(button => {
        button.addEventListener('click', function() {
            const moduleId = this.getAttribute('data-module-id');
            openEditModuleModal(moduleId);
        });
    });

    document.querySelectorAll('.delete-module-btn').forEach(button => {
        button.addEventListener('click', function() {
            const moduleId = this.getAttribute('data-module-id');
            openDeleteConfirmModal('module', moduleId);
        });
    });

    document.querySelectorAll('.add-lesson-btn').forEach(button => {
        button.addEventListener('click', function() {
            const moduleId = this.getAttribute('data-module-id');
            openAddLessonModal(moduleId);
        });
    });

    document.querySelectorAll('.edit-lesson-btn').forEach(button => {
        button.addEventListener('click', function() {
            const lessonId = this.getAttribute('data-lesson-id');
            const moduleId = this.closest('.module-card').getAttribute('data-module-id');
            openEditLessonModal(moduleId, lessonId);
        });
    });

    document.querySelectorAll('.delete-lesson-btn').forEach(button => {
        button.addEventListener('click', function() {
            const lessonId = this.getAttribute('data-lesson-id');
            openDeleteConfirmModal('lesson', lessonId);
        });
    });
}

/**
 * Render lessons for a module
 */
function renderLessons(lessons, moduleId) {
    if (!lessons || lessons.length === 0) {
        return '<p class="text-muted text-center py-3">Chưa có bài học nào trong module này</p>';
    }

    let lessonsHtml = '<ul class="lesson-list" data-module-id="' + moduleId + '">';

    lessons.forEach(lesson => {
        // Sử dụng các thuộc tính theo schema mới
        const position = lesson.position || 1;
        const isFree = lesson.isFree || false;
        const duration = lesson.duration || 0;

        lessonsHtml += `
            <li class="lesson-item" data-lesson-id="${lesson.lessonId}">
                <div class="lesson-info">
                    <i class="fas fa-grip-vertical text-muted"></i>
                    <div class="lesson-icon">
                        <i class="fas fa-book"></i>
                    </div>
                    <div>
                        <span class="lesson-title">
                            <span class="lesson-order">${position}.</span> 
                            ${lesson.title}
                            ${isFree ? '<span class="badge bg-info preview-badge">Xem thử</span>' : ''}
                        </span>
                        <div>
                            <span class="lesson-duration">${formatDuration(duration)}</span>
                        </div>
                    </div>
                </div>
                <div class="lesson-actions">
                    <button class="btn btn-sm btn-outline-primary edit-lesson-btn" 
                            data-lesson-id="${lesson.lessonId}" title="Chỉnh sửa bài học">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-lesson-btn" 
                            data-lesson-id="${lesson.lessonId}" title="Xóa bài học">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            </li>
        `;
    });

    lessonsHtml += '</ul>';
    return lessonsHtml;
}

/**
 * Initialize sortable functionality for modules and lessons
 */
function initSortable() {
    // Make modules sortable
    $('#moduleList').sortable({
        handle: '.module-header',
        placeholder: 'module-sortable-placeholder',
        forcePlaceholderSize: true,
        update: function(event, ui) {
            updateModuleOrder();
        }
    });

    // Make lessons sortable within their module
    $('.lesson-list').sortable({
        connectWith: '.lesson-list',
        placeholder: 'lesson-sortable-placeholder',
        forcePlaceholderSize: true,
        items: '.lesson-item',
        cursor: 'move',
        update: function(event, ui) {
            const moduleId = $(this).data('module-id');
            updateLessonOrder(moduleId);
        }
    });
}

/**
 * Update module order after drag and drop
 */
function updateModuleOrder() {
    const moduleCards = document.querySelectorAll('.module-card');
    let orderUpdates = [];

    moduleCards.forEach((card, index) => {
        const moduleId = card.getAttribute('data-module-id');
        const orderDisplay = card.querySelector('.module-order');
        const newOrder = index + 1;

        if (orderDisplay) {
            orderDisplay.textContent = newOrder + '.';
        }

        orderUpdates.push({
            moduleId: moduleId,
            position: newOrder
        });
    });

    // Update the orders in the backend
    fetchWithAuth(`/api/modules/${courseId}/order`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(orderUpdates)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể cập nhật thứ tự module');
            }
            return response.json();
        })
        .then(data => {
            console.log(`[${getCurrentDateTime()}] Module order updated successfully:`, data);
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error updating module order:`, error);
            showErrorMessage('Không thể cập nhật thứ tự module');
        });
}

/**
 * Update lesson order after drag and drop
 */
function updateLessonOrder(moduleId) {
    const moduleCard = document.querySelector(`.module-card[data-module-id="${moduleId}"]`);
    if (!moduleCard) return;

    const lessonItems = moduleCard.querySelectorAll('.lesson-item');
    let orderUpdates = [];

    lessonItems.forEach((item, index) => {
        const lessonId = item.getAttribute('data-lesson-id');
        const orderDisplay = item.querySelector('.lesson-order');
        const newOrder = index + 1;

        if (orderDisplay) {
            orderDisplay.textContent = newOrder + '.';
        }

        orderUpdates.push({
            lessonId: lessonId,
            position: newOrder,
            moduleId: moduleId
        });
    });

    // Update the orders in the backend
    fetchWithAuth(`/api/lessons/${moduleId}/order`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(orderUpdates)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể cập nhật thứ tự bài học');
            }
            return response.json();
        })
        .then(data => {
            console.log(`[${getCurrentDateTime()}] Lesson order updated successfully:`, data);
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error updating lesson order:`, error);
            showErrorMessage('Không thể cập nhật thứ tự bài học');
        });
}

/**
 * Open add module modal
 */
function openAddModuleModal() {
    currentModuleId = null;

    // Reset form
    document.getElementById('moduleForm').reset();
    document.getElementById('moduleId').value = '';
    document.getElementById('courseIdForModule').value = courseId;

    // Calculate next order
    const moduleCount = modules.length;
    document.getElementById('moduleOrder').value = moduleCount + 1;

    // Update modal title
    document.getElementById('moduleModalLabel').textContent = 'Thêm Module mới';

    moduleModal.show();
}

/**
 * Open edit module modal
 */
function openEditModuleModal(moduleId) {
    currentModuleId = moduleId;

    // Find the module
    const module = modules.find(m => m.moduleId.toString() === moduleId.toString());
    if (!module) {
        showErrorMessage('Không tìm thấy thông tin module');
        return;
    }

    // Fill form data
    document.getElementById('moduleId').value = module.moduleId;
    document.getElementById('courseIdForModule').value = courseId;
    document.getElementById('moduleTitle').value = module.title;
    document.getElementById('moduleDescription').value = module.description || '';
    document.getElementById('moduleOrder').value = module.orderIndex || 1;

    // Update modal title
    document.getElementById('moduleModalLabel').textContent = 'Chỉnh sửa Module';

    moduleModal.show();
}

/**
 * Open add lesson modal
 */
function openAddLessonModal(moduleId) {
    currentModuleId = moduleId;
    currentLessonId = null;

    // Reset form
    document.getElementById('lessonForm').reset();
    document.getElementById('lessonId').value = '';
    document.getElementById('moduleIdForLesson').value = moduleId;

    // Hide all type-specific fields
    document.querySelectorAll('.lesson-type-fields').forEach(field => {
        field.style.display = 'none';
    });

    // Calculate next order
    const module = modules.find(m => m.moduleId.toString() === moduleId.toString());
    if (module && module.lessons) {
        document.getElementById('lessonOrder').value = (module.lessons.length || 0) + 1;
    } else {
        document.getElementById('lessonOrder').value = 1;
    }

    // Update modal title
    document.getElementById('lessonModalLabel').textContent = 'Thêm Bài học mới';

    // Clear current document container
    document.getElementById('currentDocumentContainer').innerHTML = '';

    lessonModal.show();
}

/**
 * Open edit lesson modal
 */
function openEditLessonModal(moduleId, lessonId) {
    currentModuleId = moduleId;
    currentLessonId = lessonId;

    // Find the module and lesson
    const module = modules.find(m => m.moduleId.toString() === moduleId.toString());
    if (!module || !module.lessons) {
        showErrorMessage('Không tìm thấy thông tin module');
        return;
    }

    const lesson = module.lessons.find(l => l.lessonId.toString() === lessonId.toString());
    if (!lesson) {
        showErrorMessage('Không tìm thấy thông tin bài học');
        return;
    }

    // Fill form data
    document.getElementById('lessonId').value = lesson.lessonId;
    document.getElementById('moduleIdForLesson').value = moduleId;
    document.getElementById('lessonTitle').value = lesson.title;
    document.getElementById('lessonType').value = lesson.lessonType || '';
    document.getElementById('lessonDescription').value = lesson.description || '';
    document.getElementById('lessonOrder').value = lesson.orderIndex || 1;
    document.getElementById('isFreePreview').checked = lesson.isFreePreview || false;

    // Hide all type-specific fields first
    document.querySelectorAll('.lesson-type-fields').forEach(field => {
        field.style.display = 'none';
    });

    // Show type-specific fields based on lesson type
    if (lesson.lessonType) {
        const typeField = document.getElementById(`${lesson.lessonType.toLowerCase()}Fields`);
        if (typeField) {
            typeField.style.display = 'block';

            // Fill type-specific data
            switch (lesson.lessonType) {
                case 'VIDEO':
                    document.getElementById('videoUrl').value = lesson.videoUrl || '';
                    document.getElementById('videoDuration').value = lesson.duration || 0;
                    break;

                case 'DOCUMENT':
                    // Show current document if available
                    if (lesson.documentUrl) {
                        document.getElementById('currentDocumentContainer').innerHTML = `
                            <div class="mt-2">
                                <p class="mb-1">Tài liệu hiện tại:</p>
                                <div class="d-flex align-items-center">
                                    <i class="fas fa-file-alt me-2 text-primary"></i>
                                    <a href="${lesson.documentUrl}" target="_blank">${lesson.documentName || 'Xem tài liệu'}</a>
                                </div>
                            </div>
                        `;
                    } else {
                        document.getElementById('currentDocumentContainer').innerHTML = '';
                    }
                    break;

                case 'ASSIGNMENT':
                    document.getElementById('assignmentInstructions').value = lesson.instructions || '';
                    break;
            }
        }
    }

    // Update modal title
    document.getElementById('lessonModalLabel').textContent = 'Chỉnh sửa Bài học';

    lessonModal.show();
}

/**
 * Handle lesson type change
 */
function handleLessonTypeChange() {
    const lessonType = document.getElementById('lessonType').value;

    // Hide all type-specific fields
    document.querySelectorAll('.lesson-type-fields').forEach(field => {
        field.style.display = 'none';
    });

    // Show fields based on selected type
    if (lessonType) {
        const typeField = document.getElementById(`${lessonType.toLowerCase()}Fields`);
        if (typeField) {
            typeField.style.display = 'block';
        }
    }
}

/**
 * Open delete confirmation modal
 */
function openDeleteConfirmModal(type, id) {
    deleteType = type;
    deleteId = id;

    let message = '';

    if (type === 'module') {
        const module = modules.find(m => m.moduleId.toString() === id.toString());
        if (module) {
            message = `Bạn có chắc chắn muốn xóa module "${module.title}"?`;

            if (module.lessons && module.lessons.length > 0) {
                message += ` Module này có ${module.lessons.length} bài học sẽ bị xóa theo.`;
            }
        } else {
            message = 'Bạn có chắc chắn muốn xóa module này?';
        }
    } else if (type === 'lesson') {
        // Find the lesson
        let lessonTitle = '';
        modules.forEach(module => {
            if (module.lessons) {
                const lesson = module.lessons.find(l => l.lessonId.toString() === id.toString());
                if (lesson) {
                    lessonTitle = lesson.title;
                }
            }
        });

        message = `Bạn có chắc chắn muốn xóa bài học "${lessonTitle || 'này'}"?`;
    }

    document.getElementById('deleteConfirmMessage').textContent = message;

    deleteConfirmModal.show();
}

/**
 * Confirm delete action
 */
function confirmDelete() {
    let url = '';

    if (deleteType === 'module') {
        url = `/api/modules/${deleteId}`;
    } else if (deleteType === 'lesson') {
        url = `/api/lessons/${deleteId}`;
    } else {
        showErrorMessage('Không xác định được loại dữ liệu cần xóa');
        return;
    }

    fetchWithAuth(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể xóa ${deleteType}`);
            }
            return response.json();
        })
        .then(data => {
            deleteConfirmModal.hide();

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: `${deleteType === 'module' ? 'Module' : 'Bài học'} đã được xóa`,
                confirmButtonText: 'OK'
            }).then(() => {
                // Reload modules
                loadModules();
                if (deleteType === 'module') {
                    setTimeout(() => {
                        updateRemainingModulesOrder();
                    }, 500);
                }
                // Update completion percentage
                updateCompletionPercentage();
            });
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error deleting ${deleteType}:`, error);
            showErrorMessage(`Không thể xóa ${deleteType === 'module' ? 'module' : 'bài học'}`);
        });
}

/**
 * Save module (create or update)
 */
function saveModule() {
    // Validate form
    const title = document.getElementById('moduleTitle').value.trim();
    if (!title) {
        document.getElementById('moduleTitle').classList.add('is-invalid');
        document.getElementById('moduleTitleError').style.display = 'block';
        return;
    } else {
        document.getElementById('moduleTitle').classList.remove('is-invalid');
        document.getElementById('moduleTitleError').style.display = 'none';
    }

    // Get form data
    const moduleId = document.getElementById('moduleId').value;
    const courseId = document.getElementById('courseIdForModule').value;
    const description = document.getElementById('moduleDescription').value.trim();
    const orderIndex = parseInt(document.getElementById('moduleOrder').value) || 1;

    // Create data object
    const moduleData = {
        title: title,
        description: description,
        orderIndex: orderIndex,
        courseId: courseId
    };

    // Determine if this is an update or create
    const isUpdate = moduleId && moduleId.trim() !== '';
    const url = isUpdate ?
        `/api/modules/${moduleId}` :
        `/api/modules/${courseId}`;
    const method = isUpdate ? 'PUT' : 'POST';

    // Make API call
    fetchWithAuth(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(moduleData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể lưu thông tin module');
            }
            return response.json();
        })
        .then(data => {
            moduleModal.hide();

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: isUpdate ? 'Module đã được cập nhật' : 'Module mới đã được tạo',
                confirmButtonText: 'OK'
            }).then(() => {
                // Reload modules
                loadModules();

                // Update module status
                updateModuleStatus(true);

                // Update completion percentage
                updateCompletionPercentage();
            });
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error saving module:`, error);
            showErrorMessage('Không thể lưu thông tin module');
        });
}

/**
 * Save lesson (create or update)
 */
function saveLesson() {
    // Validate form
    const title = document.getElementById('lessonTitle').value.trim();
    const lessonType = document.getElementById('lessonType').value;

    if (!title) {
        document.getElementById('lessonTitle').classList.add('is-invalid');
        document.getElementById('lessonTitleError').style.display = 'block';
        return;
    } else {
        document.getElementById('lessonTitle').classList.remove('is-invalid');
        document.getElementById('lessonTitleError').style.display = 'none';
    }

    if (!lessonType) {
        document.getElementById('lessonType').classList.add('is-invalid');
        document.getElementById('lessonTypeError').style.display = 'block';
        return;
    } else {
        document.getElementById('lessonType').classList.remove('is-invalid');
        document.getElementById('lessonTypeError').style.display = 'none';
    }

    // Additional validation for type-specific fields
    if (lessonType === 'VIDEO') {
        const videoUrl = document.getElementById('videoUrl').value.trim();
        if (!videoUrl) {
            document.getElementById('videoUrl').classList.add('is-invalid');
            document.getElementById('videoUrlError').style.display = 'block';
            return;
        } else {
            document.getElementById('videoUrl').classList.remove('is-invalid');
            document.getElementById('videoUrlError').style.display = 'none';
        }
    }

    // Get form data
    const lessonId = document.getElementById('lessonId').value;
    const moduleId = document.getElementById('moduleIdForLesson').value;
    const description = document.getElementById('lessonDescription').value.trim();
    const orderIndex = parseInt(document.getElementById('lessonOrder').value) || 1;
    const isFreePreview = document.getElementById('isFreePreview').checked;

    // Create basic data object
    const lessonData = {
        title: title,
        description: description,
        lessonType: lessonType,
        orderIndex: orderIndex,
        moduleId: moduleId,
        isFreePreview: isFreePreview
    };

    // Add type-specific data
    if (lessonType === 'VIDEO') {
        lessonData.videoUrl = document.getElementById('videoUrl').value.trim();
        lessonData.duration = parseFloat(document.getElementById('videoDuration').value) || 0;
    } else if (lessonType === 'ASSIGNMENT') {
        lessonData.instructions = document.getElementById('assignmentInstructions').value.trim();
    }

    // Determine if this is an update or create
    const isUpdate = lessonId && lessonId.trim() !== '';
    const url = isUpdate ?
        `/api/lessons/${lessonId}` :
        `/api/lessons/${moduleId}`;
    const method = isUpdate ? 'PUT' : 'POST';

    // For document type with file upload, we need FormData
    if (lessonType === 'DOCUMENT' && document.getElementById('documentFile').files.length > 0) {
        const formData = new FormData();

        // Add basic lesson data
        for (const key in lessonData) {
            formData.append(key, lessonData[key]);
        }

        // Add file
        formData.append('documentFile', document.getElementById('documentFile').files[0]);

        // Make API call with FormData
        fetchWithAuth(url, {
            method: method,
            body: formData
        })
            .then(handleLessonResponse(isUpdate))
            .catch(handleLessonError);
    } else {
        // Regular JSON API call for other types
        fetchWithAuth(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(lessonData)
        })
            .then(handleLessonResponse(isUpdate))
            .catch(handleLessonError);
    }
}

/**
 * Handle lesson response
 */
function handleLessonResponse(isUpdate) {
    return function(response) {
        if (!response.ok) {
            throw new Error('Không thể lưu thông tin bài học');
        }
        return response.json().then(data => {
            lessonModal.hide();

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: isUpdate ? 'Bài học đã được cập nhật' : 'Bài học mới đã được tạo',
                confirmButtonText: 'OK'
            }).then(() => {
                // Reload modules
                loadModules();

                // Update completion percentage
                updateCompletionPercentage();
            });
        });
    };
}

/**
 * Handle lesson error
 */
function handleLessonError(error) {
    console.error(`[${getCurrentDateTime()}] Error saving lesson:`, error);
    showErrorMessage('Không thể lưu thông tin bài học');
}

/**
 * Update course publish status
 */
function updateCourseStatus(isPublished) {
    fetchWithAuth(`/api/courses/${courseId}/publish`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-User': 'thanhcong2001qn',
            'X-Timestamp': '2025-05-27 13:30:44'
        },
        body: JSON.stringify({ isPublished: isPublished })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể ${isPublished ? 'xuất bản' : 'hủy xuất bản'} khóa học`);
            }
            return response.json();
        })
        .then(data => {
            // Update the UI
            const statusBadge = document.getElementById('courseStatus');
            if (isPublished) {
                statusBadge.className = 'badge bg-success';
                statusBadge.textContent = 'Đã xuất bản';
                document.getElementById('coursePublishedAt').textContent = formatDate(new Date());
            } else {
                statusBadge.className = 'badge bg-secondary';
                statusBadge.textContent = 'Chưa xuất bản';
            }

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: isPublished ? 'Khóa học đã được xuất bản' : 'Khóa học đã được hủy xuất bản',
                confirmButtonText: 'OK'
            });
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error updating course status:`, error);
            showErrorMessage(`Không thể ${isPublished ? 'xuất bản' : 'hủy xuất bản'} khóa học`);
        });
}

/**
 * Toggle featured status
 */
function toggleFeatured() {
    const isFeatured = document.getElementById('toggleFeaturedBtn').textContent.includes('Bỏ đánh dấu');
    const newStatus = !isFeatured;

    fetchWithAuth(`/api/courses/${courseId}/featured`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-User': 'thanhcong2001qn',
            'X-Timestamp': '2025-05-27 13:30:44'
        },
        body: JSON.stringify({ isFeatured: newStatus })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Không thể ${newStatus ? 'đánh dấu' : 'bỏ đánh dấu'} khóa học nổi bật`);
            }
            return response.json();
        })
        .then(data => {
            // Update the UI
            if (newStatus) {
                document.getElementById('toggleFeaturedBtn').textContent = 'Bỏ đánh dấu nổi bật';
            } else {
                document.getElementById('toggleFeaturedBtn').textContent = 'Đánh dấu nổi bật';
            }

            // Show success message
            Swal.fire({
                icon: 'success',
                title: 'Thành công!',
                text: newStatus ? 'Khóa học đã được đánh dấu nổi bật' : 'Đã bỏ đánh dấu nổi bật cho khóa học',
                confirmButtonText: 'OK'
            });
        })
        .catch(error => {
            console.error(`[${getCurrentDateTime()}] Error toggling featured status:`, error);
            showErrorMessage(`Không thể ${newStatus ? 'đánh dấu' : 'bỏ đánh dấu'} khóa học nổi bật`);
        });
}

/**
 * Update module status UI
 */
function updateModuleStatus(hasModules) {
    const moduleStatusBadge = document.getElementById('modulesStatus');

    if (hasModules) {
        moduleStatusBadge.className = 'badge bg-success';
        moduleStatusBadge.innerHTML = '<i class="fas fa-check"></i>';
    } else {
        moduleStatusBadge.className = 'badge bg-danger';
        moduleStatusBadge.innerHTML = '<i class="fas fa-times"></i>';
    }

    updateCompletionPercentage();
}

/**
 * Update completion percentage
 */
function updateCompletionPercentage() {
    // Get all the statuses
    const badges = document.querySelectorAll('.badge i.fa-check');
    const completedSteps = badges.length;
    const totalSteps = 5; // Total number of steps

    // Calculate percentage
    const percentage = Math.round((completedSteps / totalSteps) * 100);

    // Update the progress bar
    const progressBar = document.getElementById('courseCompletionBar');
    progressBar.style.width = `${percentage}%`;
    progressBar.setAttribute('aria-valuenow', percentage);
    progressBar.textContent = `${percentage}%`;

    // Update text
    document.getElementById('courseCompletionText').textContent = `Hoàn thành ${completedSteps}/${totalSteps} bước`;
}

/**
 * Format date
 */
function formatDate(dateString) {
    if (!dateString) return 'N/A';

    const date = new Date(dateString);
    if (isNaN(date.getTime())) return 'N/A';

    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

/**
 * Format duration in minutes to readable format
 */
function formatDuration(minutes) {
    if (!minutes) return '0 phút';

    const hours = Math.floor(minutes / 60);
    const mins = Math.round(minutes % 60);

    if (hours > 0) {
        return `${hours} giờ ${mins > 0 ? mins + ' phút' : ''}`;
    } else {
        return `${mins} phút`;
    }
}

/**
 * Format currency (VND)
 */
function formatCurrency(amount) {
    if (amount === 0) return 'Miễn phí';

    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

/**
 * Get human-readable label for lesson type
 */
function getLessonTypeLabel(type) {
    switch(type) {
        case 'VIDEO':
            return 'Video';
        case 'DOCUMENT':
            return 'Tài liệu';
        case 'QUIZ':
            return 'Bài kiểm tra';
        case 'ASSIGNMENT':
            return 'Bài tập';
        default:
            return 'Không xác định';
    }
}

/**
 * Get icon class for lesson type
 */
function getLessonTypeIcon(type) {
    switch(type) {
        case 'VIDEO':
            return 'fas fa-play';
        case 'DOCUMENT':
            return 'fas fa-file-alt';
        case 'QUIZ':
            return 'fas fa-question-circle';
        case 'ASSIGNMENT':
            return 'fas fa-tasks';
        default:
            return 'fas fa-book';
    }
}

/**
 * Show loading indicator
 */
function showLoading(isLoading) {
    if (isLoading) {
        // You can implement a loading spinner or overlay here
        console.log(`[${getCurrentDateTime()}] Loading...`);
    } else {
        console.log(`[${getCurrentDateTime()}] Loading complete`);
    }
}

/**
 * Show error message
 */
function showErrorMessage(message) {
    Swal.fire({
        icon: 'error',
        title: 'Lỗi!',
        text: message,
        confirmButtonText: 'OK'
    });

    console.error(`[${getCurrentDateTime()}] Error: ${message}`);
}
function getCurrentDateTime() {
    return '2025-05-27 13:30:44'; // Static timestamp for consistent logging
}
function updateRemainingModulesOrder() {
    const moduleCards = document.querySelectorAll('.module-card');
    let orderUpdates = [];

    moduleCards.forEach((card, index) => {
        const moduleId = card.getAttribute('data-module-id');
        const orderDisplay = card.querySelector('.module-order');
        const newOrder = index + 1;

        if (orderDisplay) {
            orderDisplay.textContent = newOrder + '.';
        }

        orderUpdates.push({
            moduleId: moduleId,
            position: newOrder
        });
    });

    // Update the orders in the backend
    if (orderUpdates.length > 0) {
        fetchWithAuth(`/api/modules/${courseId}/order`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderUpdates)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Không thể cập nhật thứ tự module sau khi xóa');
                }
                return response.json();
            })
            .then(data => {
                console.log(`[${getCurrentDateTime()}] Module order updated after deletion:`, data);
            })
            .catch(error => {
                console.error(`[${getCurrentDateTime()}] Error updating module order after deletion:`, error);
                showErrorMessage('Không thể cập nhật thứ tự module sau khi xóa');
            });
    }
}
function loadLessonsForAllModules(modules) {

    if (!modules || modules.length === 0) {
        return Promise.resolve([]);
    }

    // Tạo mảng các Promise để tải bài học cho từng module
    const loadPromises = modules.map(module => loadLessonsForModule(module));

    // Đợi tất cả các Promise hoàn thành
    return Promise.all(loadPromises);
}
function loadLessonsForModule(module) {
    return fetchWithAuth(`/api/lessons/${module.moduleId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'X-User': 'thanhcong2001qn',
            'X-Timestamp': '2025-05-28 13:21:12'
        }
    })
        .then(response => {
            if (!response.ok) {
                console.warn(`[2025-05-28 13:21:12] Warning: Could not load lessons for module ${module.moduleId}: ${response.status}`);
                module.lessons = [];
                return module;
            }
            return response.json().then(lessons => {
                console.log(`[2025-05-28 13:21:12] Loaded ${lessons.length} lessons for module ${module.moduleId}`);
                module.lessons = lessons;
                return module;
            });
        })
        .catch(error => {
            console.error(`[2025-05-28 13:21:12] Error loading lessons for module ${module.moduleId}:`, error);
            module.lessons = [];
            return module; // Trả về module với mảng bài học rỗng để tránh lỗi
        });
}
