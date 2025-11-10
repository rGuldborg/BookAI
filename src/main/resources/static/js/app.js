const bookGrid = document.getElementById("book-grid");
const chatBox = document.getElementById("chatbox");
const chatInput = document.getElementById("chat-input");
const chatContainer = document.getElementById("chat-box");
const searchInput = document.getElementById("search");

let selectedBook = null;

// === Hent og vis bøger ===
function loadBooks(query = "books") {
    fetch(`/api/books?q=${encodeURIComponent(query)}`)
        .then(r => r.json())
        .then(renderBooks)
        .catch(err => console.error("Fejl ved hentning af bøger:", err));
}

// === Vis bøger (maks 12) ===
function renderBooks(books) {
    bookGrid.innerHTML = "";
    books.slice(0, 12).forEach(b => {
        const div = document.createElement("div");
        div.classList.add("book");
        div.innerHTML = `<img src="${b.imageUrl}" alt="${b.title}" /><p>${b.title}</p>`;
        div.onclick = () => openChat(b);
        bookGrid.appendChild(div);
    });
}

// === Søg ===
searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim() || "books";
    loadBooks(query);
});

// === Åbn chat ===
function openChat(book) {
    selectedBook = book;
    chatContainer.classList.add("visible");
    chatBox.innerHTML = `
    <p><b>${book.title}</b></p>
    <p>Hello! What would you like to know about this book?</p>
  `;
}

// === Luk chat ===
document.addEventListener("click", e => {
    if (e.target.id === "close-chat") {
        chatContainer.classList.remove("visible");
        setTimeout(() => {
            chatBox.innerHTML = "";
        }, 300);
    }
});

// === Tilføj “AI is typing…” indikator ===
function showTypingIndicator() {
    const typingDiv = document.createElement("div");
    typingDiv.classList.add("typing-indicator");
    typingDiv.innerHTML = "<span></span><span></span><span></span>";
    chatBox.appendChild(typingDiv);
    chatBox.scrollTop = chatBox.scrollHeight;
    return typingDiv;
}

// === Simuler skrivning af AI’s svar ===
function typeWriterEffect(element, text, speed = 25) {
    let i = 0;
    const interval = setInterval(() => {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            chatBox.scrollTop = chatBox.scrollHeight;
        } else {
            clearInterval(interval);
        }
    }, speed);
}

// === Send spørgsmål ===
chatInput.addEventListener("keydown", e => {
    if (e.key === "Enter" && chatInput.value.trim() !== "" && selectedBook) {
        const question = chatInput.value;
        chatBox.innerHTML += `<p><b>You:</b> ${question}</p>`;
        chatInput.value = "";

        const typingIndicator = showTypingIndicator();

        fetch(`/api/chat?book=${encodeURIComponent(selectedBook.title)}&question=${encodeURIComponent(question)}`)
            .then(r => r.json())
            .then(data => {
                typingIndicator.remove();

                const answer = data?.choices?.[0]?.message?.content || "Sorry, I couldn’t find an answer.";
                const aiMessage = document.createElement("p");
                aiMessage.innerHTML = "<b>AI:</b> ";
                chatBox.appendChild(aiMessage);

                typeWriterEffect(aiMessage, " " + answer);
            })
            .catch(err => {
                typingIndicator.remove();
                console.error("Fejl i chat:", err);
                chatBox.innerHTML += `<p style="color:#b33"><b>Error:</b> Could not get a response.</p>`;
            });
    }
});

// === Start med engelske bøger ===
loadBooks();
