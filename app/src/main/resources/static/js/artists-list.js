const artistsList = document.getElementById('artistsList');
const searchInput = document.getElementById('searchInput');
const addBtn = document.getElementById('addArtistBtn');

async function loadArtists() {
    const query = searchInput.value.trim();
    try {
        const artists = await apiPost('/api/artists/filter', {
            search: query || null,
            sortBy: 'name',
            sortDirection: 'asc'
        });

        artistsList.innerHTML = '';

        artists.forEach(artist => {
            const card = document.createElement('div');
            card.className = 'artist-card';
            card.dataset.id = artist.id;

            const avatarHtml = artist.avatarPath
                ? `<img src="/api/files/${encodeURI(artist.avatarPath)}" class="avatar" alt="">`
                : `<div class="avatar-placeholder">${artist.name.charAt(0).toUpperCase()}</div>`;

            card.innerHTML = `
                ${avatarHtml}
                <div class="info">
                    <div class="name">${artist.name}</div>
                    <div class="description">${artist.description || ''}</div>
                </div>
                <button class="edit-btn">✏️</button>
                <button class="delete-btn">🗑</button>
            `;

            card.querySelector('.avatar, .avatar-placeholder').addEventListener('click', () => {
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'image/*';
                input.addEventListener('change', async (e) => {
                    const file = e.target.files[0];
                    if (!file) return;
                    const formData = new FormData();
                    formData.append('file', file);
                    await fetch('/api/artists/' + artist.id + '/avatar', {
                        method: 'POST',
                        body: formData
                    });
                    loadArtists();
                });
                input.click();
            });

            card.querySelector('.edit-btn').addEventListener('click', () => {
                const nameEl = card.querySelector('.name');
                const descEl = card.querySelector('.description');
                const currentName = artist.name;
                const currentDesc = artist.description || '';

                nameEl.innerHTML = `<input type="text" value="${currentName}">`;
                descEl.innerHTML = `<input type="text" value="${currentDesc}" placeholder="Описание">`;

                const nameInput = nameEl.querySelector('input');
                const descInput = descEl.querySelector('input');

                let isSaving = false; // флаг для предотвращения повторных сохранений

                const save = async () => {
                    if (isSaving) return; // предотвращаем множественные вызовы
                    isSaving = true;

                    const newName = nameInput.value.trim() || currentName;
                    const newDesc = descInput.value.trim();

                    // Сохраняем только если были изменения
                    if (newName === currentName && newDesc === currentDesc) {
                        loadArtists();
                        return;
                    }

                    try {
                        await apiPut('/api/artists/' + artist.id, { name: newName, description: newDesc });
                        loadArtists();
                    } catch (e) {
                        showStatus(e.message, false);
                        isSaving = false; // сбрасываем флаг при ошибке
                    }
                };

                nameInput.addEventListener('keydown', (ev) => {
                    if (ev.key === 'Enter') {
                        ev.preventDefault();
                        save();
                    }
                });
                descInput.addEventListener('keydown', (ev) => {
                    if (ev.key === 'Enter') {
                        ev.preventDefault();
                        save();
                    }
                });

                document.addEventListener('click', function handleOutsideClick(e) {
                    if (!card.contains(e.target)) {
                        save();
                        document.removeEventListener('click', handleOutsideClick);
                    }
                });

                nameInput.focus();
            });

            card.querySelector('.delete-btn').addEventListener('click', async () => {
                if (!confirm('Удалить исполнителя?')) return;
                try {
                    await apiDelete('/api/artists/' + artist.id);
                    card.remove();
                    showStatus('Исполнитель удалён');
                } catch (e) {
                    showStatus(e.message, false);
                }
            });

            artistsList.appendChild(card);
        });
    } catch (e) {
        console.error(e);
        showStatus('Ошибка загрузки', false);
    }
}

addBtn.addEventListener('click', async () => {
    const card = document.createElement('div');
    card.className = 'artist-card';

    card.innerHTML = `
        <div class="avatar-placeholder">?</div>
        <div class="info">
            <div class="name"><input type="text" placeholder="Имя исполнителя"></div>
            <div class="description"><input type="text" placeholder="Описание"></div>
        </div>
    `;

    artistsList.prepend(card);

    const nameInput = card.querySelector('.name input');
    const descInput = card.querySelector('.description input');
    nameInput.focus();

    let isSaving = false;

    const save = async () => {
        if (isSaving) return;
        isSaving = true;

        const name = nameInput.value.trim();
        const desc = descInput.value.trim();
        if (!name) {
            isSaving = false;
            return;
        }
        try {
            await apiPost('/api/artists', { name, description: desc });
            await loadArtists();
        } catch (e) {
            showStatus(e.message, false);
            isSaving = false;
        }
    };

    nameInput.addEventListener('keydown', (ev) => {
        if (ev.key === 'Enter') {
            ev.preventDefault();
            save();
        }
    });

    descInput.addEventListener('keydown', (ev) => {
        if (ev.key === 'Enter') {
            ev.preventDefault();
            save();
        }
    });

    descInput.addEventListener('blur', save);
});

searchInput.addEventListener('input', () => loadArtists());

function showStatus(message, isSuccess = true) {
    const statusDiv = document.getElementById('status');
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

loadArtists();