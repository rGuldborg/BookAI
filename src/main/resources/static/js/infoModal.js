const modal = document.createElement("div");
modal.id = "info-modal";
modal.innerHTML = `
  <div class="modal-content">
    <span class="close-modal">&times;</span>
    <img id="modal-image" alt="Book cover" />
    <h2 id="modal-title"></h2>
    <p><strong>Author:</strong> <span id="modal-author"></span></p>
    <p><strong>Published:</strong> <span id="modal-year"></span></p>
    <div id="modal-description"></div>
  </div>
`;
document.body.appendChild(modal);

function openBookInfoModal(bookId) {
    fetch(`/api/books/${bookId}`)
        .then(r => r.json())
        .then(data => {
            document.getElementById("modal-title").textContent = data.title || "Unknown title";
            document.getElementById("modal-author").textContent = data.author || "Unknown author";
            document.getElementById("modal-year").textContent = data.publishedDate || "Unknown";
            document.getElementById("modal-image").src = data.imageUrl || "";

            const descDiv = document.getElementById("modal-description");
            descDiv.innerHTML = data.description
                ? data.description
                : "<em>No description available.</em>";

            modal.classList.add("visible");
        })
        .catch(err => console.error("Fejl ved hentning af boginfo:", err));
}

modal.querySelector(".close-modal").onclick = () => modal.classList.remove("visible");
window.onclick = (e) => { if (e.target === modal) modal.classList.remove("visible"); };
