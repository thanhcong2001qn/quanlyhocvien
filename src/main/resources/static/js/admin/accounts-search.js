//document.addEventListener('DOMContentLoaded', function() {
//    const searchForm = document.getElementById('searchForm');
//    const searchInput = document.getElementById('searchInput');
//    const roleFilter = document.getElementById('roleFilter');
//    const statusFilter = document.getElementById('statusFilter');
//    let searchTimeout;
//
//    function debounce(func, wait) {
//        return function executedFunction(...args) {
//            const later = () => {
//                clearTimeout(searchTimeout);
//                func(...args);
//            };
//            clearTimeout(searchTimeout);
//            searchTimeout = setTimeout(later, wait);
//        };
//    }
//
//    function handleSearch() {
//        const currentParams = new URLSearchParams(window.location.search);
//        const searchParams = new URLSearchParams();
//
//        const searchValue = searchInput.value.trim();
//        const roleValue = roleFilter.value;
//        const statusValue = statusFilter.value;
//
//        if (searchValue) searchParams.set('keyword', searchValue);
//        if (roleValue) searchParams.set('role', roleValue);
//        if (statusValue) searchParams.set('status', statusValue);
//
//        // Keep existing parameters except page
//        for (const [key, value] of currentParams.entries()) {
//            if (key !== 'page' &&
//                key !== 'keyword' &&
//                key !== 'role' &&
//                key !== 'status') {
//                searchParams.set(key, value);
//            }
//        }
//
//        // Reset to page 1 for new searches
//        searchParams.set('page', '1');
//
//        // Navigate to search URL
//        const searchUrl = `${window.location.pathname}?${searchParams.toString()}`;
//        window.location.href = searchUrl;
//    }
//
//    const debouncedSearch = debounce(() => {
//        if (searchInput.value.trim().length > 0) {
//            handleSearch();
//        }
//    }, 500);
//
//    // Event listeners
//    searchInput.addEventListener('input', debouncedSearch);
//    roleFilter.addEventListener('change', handleSearch);
//    statusFilter.addEventListener('change', handleSearch);
//
//    searchForm.addEventListener('submit', function(e) {
//        e.preventDefault();
//        handleSearch();
//    });
//
//    searchInput.addEventListener('keypress', function(e) {
//        if (e.key === 'Enter') {
//            e.preventDefault();
//            handleSearch();
//        }
//    });
//
//    // Clear search
//    document.querySelector('.clear-search').addEventListener('click', function(e) {
//        e.preventDefault();
//        searchInput.value = '';
//        roleFilter.value = '';
//        statusFilter.value = '';
//        handleSearch();
//    });
//
//    // Initialize form with URL parameters
//    const urlParams = new URLSearchParams(window.location.search);
//    searchInput.value = urlParams.get('keyword') || '';
//    roleFilter.value = urlParams.get('role') || '';
//    statusFilter.value = urlParams.get('status') || '';
//});