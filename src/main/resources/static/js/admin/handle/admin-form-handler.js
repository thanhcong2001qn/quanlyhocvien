document.addEventListener("DOMContentLoaded", function () {
    console.log("🔥 JS loaded");
    const adminForm = document.getElementById("adminForm");
    const popup = document.getElementById("popup");
    const messagePopup = document.getElementById("message-popup-notification");
    const closePopupBtn = document.getElementById("closePopupButton");
    const fileInput = document.getElementById("adminImage");
    const fileNameDisplay = document.getElementById("selectedFileName");

    // Hiển thị tên file được chọn
    fileInput.addEventListener("change", function () {
        const fileName = fileInput.files[0]?.name || "Chưa chọn file";
        fileNameDisplay.innerHTML = `<i class="fas fa-file-image me-1"></i>${fileName}`;
    });

    // Submit form bằng fetch
    adminForm.addEventListener("submit", async function (e) {
        e.preventDefault();
        console.log("✅ Submit intercepted");

        const formData = new FormData(adminForm);

        try {
            const response = await fetchWithAuth("/admin/apiAddAdmin", {
                method: "POST",
                body: formData
            });

            const result = await response.json();

            if (response.ok) {
                messagePopup.textContent = "Thêm Admin thành công!";
                popup.style.display = "block";
                adminForm.reset();
                fileNameDisplay.innerHTML = `<i class="fas fa-file-image me-1"></i>Chưa chọn file`;
            } else {
                const message = result?.message || "Đã xảy ra lỗi!";
                Swal.fire({
                    icon: "error",
                    title: "Lỗi",
                    text: message
                });
            }
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "Lỗi không xác định",
                text: error.message
            });
        }
    });

    closePopupBtn.addEventListener("click", function () {
        popup.style.display = "none";
    });
});
