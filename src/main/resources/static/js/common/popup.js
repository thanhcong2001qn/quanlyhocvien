document.addEventListener('DOMContentLoaded', function () {
    let confirmAction = null;
    let confirmModalInstance = null;

    window.showConfirmPopup = function (message, callback) {
        const modalElement = document.getElementById('confirmPopup');
        const messageElement = document.getElementById('confirmPopupMessage');

        if (!modalElement || !messageElement) {
            console.error('❌ Không tìm thấy phần tử confirm popup');
            return;
        }

        messageElement.innerText = message;
        confirmAction = callback;

        if (!confirmModalInstance) {
            confirmModalInstance = new bootstrap.Modal(modalElement);
        }
        confirmModalInstance.show();
    };

    // Xử lý nút Xác nhận
    const confirmButton = document.getElementById('confirmPopupBtn');
    if (confirmButton) {
        confirmButton.addEventListener('click', function () {
            if (confirmAction) {
                confirmAction();
            }
            if (confirmModalInstance) {
                confirmModalInstance.hide();
            }
        });
    }

    // Xử lý nút Hủy (Cancel)
    const cancelButton = document.getElementById('cancelPopupBtn');
    if (cancelButton) {
        cancelButton.addEventListener('click', function () {
            if (confirmModalInstance) {
                confirmModalInstance.hide();
            }
        });
    }

    // Khi modal đóng hoàn toàn thì chỉ reset biến
    const modalElement = document.getElementById('confirmPopup');
    if (modalElement) {
        modalElement.addEventListener('hidden.bs.modal', function () {
            confirmModalInstance = null;  // ✅ chỉ reset biến thôi
            confirmAction = null;
        });
    }
});
