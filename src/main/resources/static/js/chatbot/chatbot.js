document.addEventListener('DOMContentLoaded', () => {
    // --- PHẦN 1: DOM Elements
    const openBtn = document.getElementById('openChatBtn');
    const chatContainer = document.getElementById('chatContainer');
    const closeBtn = document.getElementById('closeChatBtn');
    const toggleSizeBtn = document.getElementById('toggleSizeBtn');
    const minimizeBtn = document.getElementById('minimizeBtn');
    const clearHistoryBtn = document.getElementById('clearHistoryBtn');
    const chatForm = document.getElementById('chat-form');
    const userInput = document.getElementById('user-input');
    const chatMessages = document.getElementById('chat-messages');
    const suggestionChips = document.querySelectorAll('.suggestion-chip');
    // Modal xác nhận xóa lịch sử
    const confirmModal = document.getElementById('chatbot-confirm-modal');
    const confirmOk = document.getElementById('chatbot-confirm-ok');
    const confirmCancel = document.getElementById('chatbot-confirm-cancel');
    const confirmBackdrop = confirmModal ? confirmModal.querySelector('.chatbot-modal-backdrop') : null;

    // --- PHẦN 2: State Management
    let typingIndicator = null;
    let isWaitingForBot = false;
    let isMinimized = false;

    // --- PHẦN 3: Chat Container Controls
    openBtn.addEventListener('click', () => {
        if (isMinimized) {
            chatContainer.style.display = 'flex';
            isMinimized = false;
        } else {
            chatContainer.style.display = chatContainer.style.display === 'flex' ? 'none' : 'flex';
        }
    });

    closeBtn.addEventListener('click', () => {
        chatContainer.style.display = 'none';
    });

    toggleSizeBtn.addEventListener('click', () => {
        chatContainer.classList.toggle('expanded');
        const icon = toggleSizeBtn.querySelector('i');
        icon.classList.toggle('fa-expand');
        icon.classList.toggle('fa-compress');
    });

    // --- PHẦN 4: Clear History (dùng modal custom)
    clearHistoryBtn.addEventListener('click', () => {
        showConfirmModal();
    });

    if (confirmOk) {
        confirmOk.addEventListener('click', () => {
            // Xóa lịch sử khỏi session storage
            localStorage.removeItem('chat_history');
            // Xóa tất cả tin nhắn khỏi UI
            chatMessages.innerHTML = '';
            // Thêm lại tin nhắn chào mừng
            addMessageToChat('bot', 'Xin chào! Tôi là trợ lý học viên. Tôi có thể giúp gì cho bạn?', false);
            hideConfirmModal();
        });
    }
    if (confirmCancel) {
        confirmCancel.addEventListener('click', hideConfirmModal);
    }
    if (confirmBackdrop) {
        confirmBackdrop.addEventListener('click', hideConfirmModal);
    }
    // Đóng modal bằng phím Esc
    document.addEventListener('keydown', (e) => {
        if (confirmModal && confirmModal.style.display === 'block' && e.key === 'Escape') {
            hideConfirmModal();
        }
    });

    function showConfirmModal() {
        if (!confirmModal) return;
        confirmModal.style.display = 'flex'; // dùng 'flex' mới align được
        document.body.classList.add('modal-open');
    }

    function hideConfirmModal() {
        if (!confirmModal) return;
        confirmModal.style.display = 'none';
        document.body.classList.remove('modal-open');
    }

    // --- PHẦN 5: Suggestion Chips
    suggestionChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const question = chip.textContent;
            userInput.value = question;
            chatForm.dispatchEvent(new Event('submit'));
        });
    });

    // --- PHẦN 6: Message Handling
    const history = loadChatFromSession();
    if (history.length > 0) {
        history.forEach(msg => addMessageToChat(msg.sender, msg.raw, false));
    } else {
        addMessageToChat('bot', 'Xin chào! Tôi là trợ lý học viên. Tôi có thể giúp gì cho bạn?', false);
    }

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (isWaitingForBot) return;

        const userMessage = userInput.value.trim();
        if (!userMessage) return;

        addMessageToChat('user', userMessage);
        userInput.value = '';
        isWaitingForBot = true;
        typingIndicator = addTypingIndicator();

        try {
            const response = await fetch('/api/chatbot/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question: userMessage })
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();

            if (typingIndicator) typingIndicator.remove();
            addMessageToChat('bot', data.response);

        } catch (error) {
            if (typingIndicator) typingIndicator.remove();
            addMessageToChat('bot', 'Xin lỗi, tôi đã gặp lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.');
            console.error('Lỗi:', error);
        } finally {
            isWaitingForBot = false;
        }
    });

    // --- PHẦN 7: Helper Functions
    function addMessageToChat(sender, message, shouldSave = true) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', sender + '-message');

        const time = getCurrentTime();

        const messageContent = document.createElement('div');
        messageContent.classList.add('message-content');

        // Xóa icon user, chỉ để avatar bot nếu là bot
        let avatar = null;
        if (sender === 'bot') {
            avatar = document.createElement('div');
            avatar.classList.add('message-avatar');
            avatar.innerHTML = '<i class="fas fa-robot"></i>';
        }

        const textContainer = document.createElement('div');
        textContainer.classList.add('message-text');
        
        if (sender === 'bot') {
            textContainer.innerHTML = message;
        } else {
            textContainer.textContent = message;
        }

        const timeElement = document.createElement('div');
        timeElement.classList.add('message-time');
        timeElement.textContent = time;

        messageContent.appendChild(textContainer);
        messageContent.appendChild(timeElement);
        if (avatar) messageElement.appendChild(avatar);
        messageElement.appendChild(messageContent);

        chatMessages.appendChild(messageElement);
        scrollToBottom();

        if (shouldSave) {
            const history = loadChatFromSession();
            history.push({ sender, raw: message });
            localStorage.setItem('chat_history', JSON.stringify(history));
        }
    }

    function getCurrentTime() {
        const now = new Date();
        const hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    function addTypingIndicator() {
        const typingElement = document.createElement('div');
        typingElement.classList.add('message', 'bot-message');
        typingElement.innerHTML = `
            <div class="message-avatar">
                <i class="fas fa-robot"></i>
            </div>
            <div class="message-content">
                <div class="typing-indicator">
                    <span></span><span></span><span></span>
                </div>
            </div>
        `;
        chatMessages.appendChild(typingElement);
        scrollToBottom();
        return typingElement;
    }

    function scrollToBottom() {
        chatMessages.scrollTo({
            top: chatMessages.scrollHeight,
            behavior: 'smooth'
        });
    }

    function loadChatFromSession() {
        const raw = localStorage.getItem('chat_history');
        try {
            const parsed = raw ? JSON.parse(raw) : [];
            return parsed;
        } catch (e) {
            sessionStorage.removeItem('chat_history');
            return [];
        }
    }

    // --- PHẦN 8: Keyboard Shortcuts
    document.addEventListener('keydown', (e) => {
        // Alt + C để mở/đóng chat
        if (e.altKey && e.key === 'c') {
            e.preventDefault();
            openBtn.click();
        }
        
        // Esc để đóng chat khi đang mở
        if (e.key === 'Escape' && chatContainer.style.display === 'flex') {
            closeBtn.click();
        }
    });

    // --- PHẦN 9: Input Focus Management
    userInput.addEventListener('focus', () => {
        if (isMinimized) {
            openBtn.click();
        }
    });
});