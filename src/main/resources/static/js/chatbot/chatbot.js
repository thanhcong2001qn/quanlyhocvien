document.addEventListener('DOMContentLoaded', () => {
    const chatForm = document.getElementById('chat-form');
    const userInput = document.getElementById('user-input');
    const chatMessages = document.getElementById('chat-messages');

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const userMessage = userInput.value.trim();
        if (!userMessage) return;

        // Thêm tin nhắn người dùng vào chat
        addMessageToChat('user', userMessage);
        userInput.value = '';

        // Hiển thị chỉ báo đang nhập
        const typingIndicator = addTypingIndicator();

        try {
            const response = await fetch('/api/chatbot/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question: userMessage })
            });

            const data = await response.json();

            // Xóa chỉ báo đang nhập
            typingIndicator.remove();

            // Thêm phản hồi từ bot vào chat
            addMessageToChat('bot', data.response);
        } catch (error) {
            // Xóa chỉ báo đang nhập
            typingIndicator.remove();

            // Thêm thông báo lỗi
            addMessageToChat('bot', 'Xin lỗi, tôi đã gặp lỗi khi xử lý yêu cầu của bạn.');
            console.error('Lỗi:', error);
        }
    });

    function addMessageToChat(sender, message) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', sender + '-message');
        messageElement.innerHTML = `
            <div class="message-content">
                <p>${message}</p>
            </div>
        `;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function addTypingIndicator() {
        const typingElement = document.createElement('div');
        typingElement.classList.add('message', 'bot-message', 'typing-indicator');
        typingElement.innerHTML = `
            <div class="message-content">
                <div class="typing-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
        `;
        chatMessages.appendChild(typingElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return typingElement;
    }
});