// js/common/popup.js
document.addEventListener('DOMContentLoaded', function () {
    let confirmAction = null;

    window.showConfirmPopup = function (message, callback) {
        document.getElementById('confirmPopupMessage').innerText = message;
        confirmAction = callback;

        // Hiển thị popup
        const confirmModal = new bootstrap.Modal(document.getElementById('confirmPopup'));
        confirmModal.show();
    };

    document.getElementById('confirmPopupBtn').addEventListener('click', function () {
        if (confirmAction) {
            confirmAction();  // Gọi hàm callback khi nhấn nút "Xác nhận"
        }
        const confirmModal = bootstrap.Modal.getInstance(document.getElementById('confirmPopup'));
        confirmModal.hide();
    });
});
