document.addEventListener('DOMContentLoaded', function () {
  const toggle = document.getElementById('theme-toggle');

  function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);

    // Đảm bảo icon luôn được cập nhật đúng mỗi lần chuyển
    const icon = document.querySelector('.theme-switch .slider i');
    if (icon) {
      icon.classList.remove('fa-sun', 'fa-moon');
      icon.classList.add(theme === 'dark' ? 'fa-moon' : 'fa-sun');
    }
  }

  // Gán theme hiện tại khi load trang
  const currentTheme = localStorage.getItem('theme') || 'light';
  toggle.checked = currentTheme === 'dark';
  setTheme(currentTheme);

  // Khi người dùng bật tắt switch
  toggle.addEventListener('change', function () {
    const newTheme = this.checked ? 'dark' : 'light';
    setTheme(newTheme);
  });
});
