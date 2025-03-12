function showPopupNotification(text) {
    // Get container and handle potential null/undefined cases
    let container = document.getElementsByClassName('container')[0];
    if (container) {
        container.style.opacity = '0.5';
        container.style.pointerEvents = 'none';
    }

    // Set popup message
    let messageElement = document.getElementById('message-popup-notification');
    if (messageElement) {
        messageElement.innerHTML = text;
    }

    // Show popup
    let popup = document.getElementById('popup');
    if (popup) {
        popup.style.display = 'block';

        // Remove any existing event listeners to prevent duplicates
        const closeButton = document.getElementById('closePopupButton');
        if (closeButton) {
            const newButton = closeButton.cloneNode(true);
            closeButton.parentNode.replaceChild(newButton, closeButton);

            // Add event listener to the new button
            newButton.addEventListener('click', () => {
                if (container) {
                    container.style.opacity = '1';
                    container.style.pointerEvents = 'auto';
                }
                popup.style.display = 'none';
            });
        }
    }
}

function showPopupSelect(text) {
    return new Promise((resolve) => {
        let container = document.getElementsByClassName('container')[0];
        if (container) {
            container.style.opacity = '0.5';
            container.style.pointerEvents = 'none';
        }
        let messageElement = document.getElementById('message-popup-select');
        if (messageElement) {
            messageElement.innerHTML = text;
        }
        let popup = document.getElementById('popup-select');
        if (popup) {
            popup.style.display = 'block';
            const closeButton = document.getElementById('noPopupButton');
            closeButton.addEventListener('click', () => {
                if (container) {
                    container.style.opacity = '1';
                    container.style.pointerEvents = 'auto';
                }
                popup.style.display = 'none';
                return resolve(false);
            })
            const yesButton = document.getElementById('yesPopupButton');
            yesButton.addEventListener('click', () => {
                if (container) {
                    container.style.opacity = '1';
                    container.style.pointerEvents = 'auto';
                }
                popup.style.display = 'none';
                return resolve(true);
            })

        }
    })
}