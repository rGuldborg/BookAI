const bookGrid = document.getElementById("book-grid");
const chatPanel = document.getElementById("chat-panel");
const chatBox = document.getElementById("chatbox");
const chatInput = document.getElementById("chat-input");
const searchInput = document.getElementById("search");

let selectedBook = null;

function loadBooks(query = "books") {
    fetch(`/api/books?q=${encodeURIComponent(query)}`)
        .then(r => r.json())
        .then(renderBooks)
        .catch(err => console.error("Fejl ved hentning af bøger:", err));
}

function renderBooks(books) {
    bookGrid.innerHTML = "";
    books.forEach(b => {
        const div = document.createElement("div");
        div.classList.add("book");
        div.innerHTML = `<img src="${b.imageUrl}" alt="${b.title}" /><p>${b.title}</p>`;
        div.onclick = () => openChat(b);
        bookGrid.appendChild(div);
    });
}

searchInput.addEventListener("input", () => {
    const query = searchInput.value.trim() || "books";
    loadBooks(query);
});

function openChat(book) {
    selectedBook = book;
    chatPanel.classList.add("visible");
    chatBox.innerHTML = `
    <p><b>${book.title}</b></p>
    <p>Hello! What would you like to know about this book?</p>
  `;
}

document.addEventListener("click", e => {
    if (e.target.id === "close-chat") {
        chatPanel.classList.remove("visible");
        setTimeout(() => {
            chatBox.innerHTML = "";
        }, 300);
    }
});

chatInput.addEventListener("keydown", e => {
    if (e.key === "Enter" && chatInput.value.trim() !== "" && selectedBook) {
        const question = chatInput.value;
        chatBox.innerHTML += `<p><b>You:</b> ${question}</p>`;
        chatInput.value = "";

        fetch(`/api/chat?book=${encodeURIComponent(selectedBook.title)}&question=${encodeURIComponent(question)}`)
            .then(r => r.json())
            .then(data => {
                const answer = data?.choices?.[0]?.message?.content || "Sorry, I couldn’t find an answer.";
                chatBox.innerHTML += `<p><b>AI:</b> ${answer}</p>`;
                chatBox.scrollTop = chatBox.scrollHeight;
            })
            .catch(err => {
                console.error("Fejl i chat:", err);
                chatBox.innerHTML += `<p style="color:#b33"><b>Error:</b> Could not get a response.</p>`;
            });
    }
});

loadBooks();
