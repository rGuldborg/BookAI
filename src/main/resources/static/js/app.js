const bookGrid = document.getElementById("book-grid");
const chatBox = document.getElementById("chatbox");
const chatInput = document.getElementById("chat-input");
const chatContainer = document.getElementById("chat-box");
const searchInput = document.getElementById("search");

let selectedBook = null;

function scrollChatToBottom() {
    requestAnimationFrame(() => {
        chatBox.scrollTop = chatBox.scrollHeight;
    });
}

function api(url) {
    return fetch(url).then(r => r.json());
}

function loadBooks(query = "books") {
    api(`/api/books?q=${encodeURIComponent(query)}`)
        .then(renderBooks)
        .catch(err => console.error("Error fetching books:", err));
}

function renderBooks(books) {
    bookGrid.innerHTML = "";
    books.slice(0, 12).forEach(b => {
        const div = document.createElement("div");
        div.classList.add("book");
        div.innerHTML = `
            <div class="book-image-wrapper">
                <img src="${b.imageUrl}" alt="${b.title}" />
                <button class="info-btn" title="Book info" data-id="${b.id}">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="#1a73e8" width="16" height="16">
                        <circle cx="12" cy="12" r="10" stroke="#1a73e8" stroke-width="1.5"></circle>
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 8h.01M11 12h1v4h1" />
                    </svg>
                </button>
            </div>
            <p>${b.title}</p>
        `;

        div.querySelector("img").onclick = () => openChat(b);
        div.querySelector(".info-btn").onclick = e => {
            e.stopPropagation();
            openBookInfoModal(b.id);
        };

        bookGrid.appendChild(div);
    });
}

searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim() || "books";
    loadBooks(query);
});

function openChat(book) {
    selectedBook = book;
    chatContainer.classList.add("visible");
    const placeholder = document.getElementById("chat-placeholder");
    if (placeholder) placeholder.classList.add("hidden");
    chatInput.style.display = "block";
    document.getElementById("close-chat").style.display = "block";
    chatBox.innerHTML = "";

    const aiMessage = document.createElement("div");
    aiMessage.classList.add("chat-message", "ai");
    aiMessage.innerHTML = `<b>${book.title}</b><br>Hello! What would you like to know about this book?`;
    chatBox.appendChild(aiMessage);

    scrollChatToBottom();
}

document.addEventListener("click", e => {
    if (e.target.id === "close-chat") {
        chatContainer.classList.remove("visible");
        const placeholder = document.getElementById("chat-placeholder");
        if (placeholder) placeholder.classList.remove("hidden");
        chatBox.innerHTML = "";
        e.target.style.display = "none";
        chatInput.style.display = "none";
        selectedBook = null;
    }
});

function showTypingIndicator() {
    const typingDiv = document.createElement("div");
    typingDiv.classList.add("typing-indicator");
    typingDiv.innerHTML = "<span></span><span></span><span></span>";
    chatBox.appendChild(typingDiv);
    scrollChatToBottom();
    return typingDiv;
}

async function typeText(el, text, speed = 20) {
    for (let char of text) {
        el.innerHTML += char;
        await new Promise(r => setTimeout(r, speed));
        scrollChatToBottom();
    }
}

async function sendMessage() {
    const question = chatInput.value.trim();
    if (!question || !selectedBook) return;

    const userMsg = document.createElement("div");
    userMsg.classList.add("chat-message", "user");
    userMsg.innerHTML = `<b>You:</b> ${question}`;
    chatBox.appendChild(userMsg);
    chatInput.value = "";
    scrollChatToBottom();

    const typing = showTypingIndicator();

    try {
        const data = await api(`/api/chat?book=${encodeURIComponent(selectedBook.title)}&question=${encodeURIComponent(question)}`);
        typing.remove();

        const answer = data?.choices?.[0]?.message?.content || "Sorry, I couldn’t find an answer.";

        const aiMsg = document.createElement("div");
        aiMsg.classList.add("chat-message", "ai");
        aiMsg.innerHTML = "<b>AI:</b> ";
        chatBox.appendChild(aiMsg);

        await typeText(aiMsg, " " + answer);
    } catch {
        typing.remove();
        const errorMsg = document.createElement("div");
        errorMsg.classList.add("chat-message", "ai");
        errorMsg.style.color = "#b33";
        errorMsg.innerHTML = `<b>Error:</b> Could not get a response.`;
        chatBox.appendChild(errorMsg);
        scrollChatToBottom();
    }
}

chatInput.addEventListener("keydown", e => {
    if (e.key === "Enter") sendMessage();
});

loadBooks();
