document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chat-form');
    const userInput = document.getElementById('user-input');
    const chatMessages = document.getElementById('chat-messages');

    let typingIndicator = null;
    let isWaitingForBot = false; // << FLAG chặn người dùng gửi tiếp

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (isWaitingForBot) return; // << Nếu đang chờ bot, không cho submit

        const userMessage = userInput.value.trim();
        if (!userMessage) return;

        // Thêm tin nhắn người dùng vào chat
        addMessageToChat('user', userMessage);
        userInput.value = '';

        // Gửi tin xong, KHÓA không cho gửi tiếp
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

            const data = await response.json();

            if (typingIndicator) {
                typingIndicator.remove();
                typingIndicator = null;
            }

            addMessageToChat('bot', data.response);

        } catch (error) {
            if (typingIndicator) {
                typingIndicator.remove();
                typingIndicator = null;
            }
            addMessageToChat('bot', 'Xin lỗi, tôi đã gặp lỗi khi xử lý yêu cầu của bạn.');
            console.error('Lỗi:', error);
        } finally {
            isWaitingForBot = false; // << Mở khóa cho phép gửi tiếp sau khi bot trả lời
        }
    });

    function addMessageToChat(sender, message) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', sender + '-message');

        const time = getCurrentTime();
        messageElement.innerHTML = `
            <div class="message-content" title="${time}">
                <div class="message-bubble">${message}</div>
                <div class="message-time">${time}</div>
            </div>
        `;

        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
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
          <div class="message-content">
            <div class="typing-indicator-container">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        `;
        chatMessages.appendChild(typingElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return typingElement;
    }

    // Update thời gian cho tin nhắn bot mặc định
    const defaultBotMessageTime = document.querySelector('#chat-messages .message-time');
    if (defaultBotMessageTime) {
        const now = new Date();
        const hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes().toString().padStart(2, '0');
        defaultBotMessageTime.textContent = `${hours}:${minutes}`;
        defaultBotMessageTime.parentElement.setAttribute('title', `${hours}:${minutes}`);
    }
});
