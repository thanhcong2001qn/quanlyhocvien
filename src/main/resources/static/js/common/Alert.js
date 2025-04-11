// public/js/AlertUtils.js
class AlertUtils {
    static success(message = 'Thành công!', title = 'Thao tác thành công') {
        Swal.fire({
            icon: 'success',
            title: title,
            text: message,
            confirmButtonText: 'OK'
        });
    }

    static error(message = 'Đã xảy ra lỗi!', title = 'Lỗi') {
        Swal.fire({
            icon: 'error',
            title: title,
            text: message,
            confirmButtonText: 'Thử lại'
        });
    }

    static confirm(message = 'Bạn có chắc chắn không?', title = 'Xác nhận', confirmCallback) {
        Swal.fire({
            title: title,
            text: message,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed && typeof confirmCallback === 'function') {
                confirmCallback();
            }
        });
    }

    static warning(message = 'Cảnh báo!', title = 'Chú ý') {
        Swal.fire({
            icon: 'warning',
            title: title,
            text: message,
            confirmButtonText: 'OK'
        });
    }

    static info(message = 'Thông tin', title = 'Thông báo') {
        Swal.fire({
            icon: 'info',
            title: title,
            text: message,
            confirmButtonText: 'OK'
        });
    }
}
window.Alert = AlertUtils;