const statusDiv = document.getElementById('status');
let currentRelease = null;
let allTracks = [];
let loading = false;

function showStatus(message, isSuccess = true) {
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

// ========== ЗАГРУЗКА ==========
async function loadRelease() {
    if (loading) return;
    loading = true;
    try {
        currentRelease = await apiGet('/api/releases/' + releaseId);
        renderRelease();
        loadArtists();
        renderTracklist();
        renderAttachments();
    } catch (e) {
        showStatus('Ошибка загрузки релиза', false);
    } finally {
        loading = false;
    }
}

function renderRelease() {
    const titleInput = document.getElementById('releaseTitle');
    const descInput = document.getElementById('descriptionInput');
    const typeSelect = document.getElementById('typeSelect');
    const coverContainer = document.getElementById('coverContainer');

    if (titleInput) titleInput.value = currentRelease.title || '';
    if (descInput) descInput.value = currentRelease.description || '';
    if (typeSelect) typeSelect.value = currentRelease.type || 'SINGLE';

    if (coverContainer) {
        if (currentRelease.coverUrl || currentRelease.coverPath) {
            const cover = currentRelease.coverUrl || currentRelease.coverPath;
            coverContainer.innerHTML = `<img src="/api/files/${encodeURI(cover)}" alt="cover">`;
        } else {
            coverContainer.innerHTML = '<div class="cover-placeholder">Нет обложки</div>';
        }
    }
}

// ========== АВТОСОХРАНЕНИЕ ==========
['releaseTitle', 'descriptionInput'].forEach(id => {
    document.getElementById(id)?.addEventListener('blur', saveRelease);
});
document.getElementById('typeSelect')?.addEventListener('change', saveRelease);

async function saveRelease() {
    const title = document.getElementById('releaseTitle').value;
    const description = document.getElementById('descriptionInput').value;
    const type = document.getElementById('typeSelect').value;
    try {
        await apiPut('/api/releases/' + releaseId, { title, description, type });
        await loadRelease();
        showStatus('Сохранено');
    } catch (e) {
        showStatus(e.message, false);
    }
}

// ========== DISCOGS ==========
let titleTimeout;
document.getElementById('releaseTitle')?.addEventListener('input', () => {
    clearTimeout(titleTimeout);
    titleTimeout = setTimeout(async () => {
        const title = document.getElementById('releaseTitle').value.trim();
        if (!title) return;
        try {
            const data = await apiGet('/api/discogs/check-release?title=' + encodeURIComponent(title));
            const fb = document.getElementById('titleFeedback');
            if (fb) {
                fb.textContent = data.warning;
                fb.style.display = 'block';
                setTimeout(() => { fb.style.display = 'none'; }, 3000);
            }
        } catch (e) {}
    }, 600);
});

// ========== ОБЛОЖКА ==========
document.getElementById('coverInput')?.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
        await fetch('/api/releases/' + releaseId + '/cover', {
            method: 'POST',
            body: formData
        });
        await loadRelease();
        showStatus('Обложка обновлена');
    } catch (e) {
        showStatus('Ошибка загрузки', false);
    }
});

// ========== ИСПОЛНИТЕЛИ ==========
let allArtists = [];
let selectedArtistIds = [];

async function loadArtists() {
    try {
        allArtists = await apiGet('/api/artists');
        selectedArtistIds = (currentRelease.artists || []).map(a => a.id);
        renderSelectedArtists();
    } catch (e) {}
}

function renderSelectedArtists() {
    const container = document.getElementById('selectedArtists');
    container.innerHTML = selectedArtistIds.map(id => {
        const artist = allArtists.find(a => a.id === id);
        if (!artist) return '';
        return `
            <div class="selected-artist-chip">
                <span>${artist.name}</span>
                <span class="remove-artist" data-id="${id}">×</span>
            </div>`;
    }).join('');

    container.querySelectorAll('.remove-artist').forEach(el => {
        el.addEventListener('click', async () => {
            selectedArtistIds = selectedArtistIds.filter(id => id !== el.dataset.id);
            await saveArtists();
        });
    });
}

async function saveArtists() {
    try {
        const data = await apiPut('/api/releases/' + releaseId + '/artists', { artistIds: selectedArtistIds });
        currentRelease.artists = data.artists;
        renderSelectedArtists();
        renderArtistDropdown();
    } catch (e) {
        showStatus(e.message, false);
    }
}

function renderArtistDropdown(filter = '') {
    const dropdown = document.getElementById('artistDropdown');
    const query = filter.toLowerCase();
    const available = allArtists.filter(a =>
        a.name.toLowerCase().includes(query)
    );

    if (available.length === 0) {
        dropdown.style.display = 'none';
        return;
    }

    dropdown.innerHTML = available.map(a => {
        const isSelected = selectedArtistIds.includes(a.id);
        return `
            <div class="artist-dropdown-item ${isSelected ? 'selected' : ''}" data-id="${a.id}">
                ${a.name} ${isSelected ? '✓' : ''}
            </div>`;
    }).join('');

    dropdown.style.display = 'block';

    dropdown.querySelectorAll('.artist-dropdown-item').forEach(item => {
        item.addEventListener('click', async () => {
            const id = item.dataset.id;
            if (selectedArtistIds.includes(id)) {
                selectedArtistIds = selectedArtistIds.filter(x => x !== id);
            } else {
                selectedArtistIds.push(id);
            }
            await saveArtists();
        });
    });
}

document.getElementById('artistSearchInput')?.addEventListener('input', () => {
    renderArtistDropdown(document.getElementById('artistSearchInput').value);
});

document.getElementById('artistSearchInput')?.addEventListener('focus', () => {
    renderArtistDropdown(document.getElementById('artistSearchInput').value);
});

document.addEventListener('click', (e) => {
    if (!e.target.closest('.artist-search')) {
        document.getElementById('artistDropdown').style.display = 'none';
    }
});

// ========== ТРЕКЛИСТ ==========
let draggedTrackId = null;

function renderTracklist() {
    const container = document.getElementById('tracklist');
    if (!container) return;
    const tracks = currentRelease.tracks || [];

    const currentTrackIds = Array.from(container.querySelectorAll('.track-row'))
        .map(row => row.dataset.trackId)
        .join(',');
    const newTrackIds = tracks.map(t => t.id).join(',');

    if (currentTrackIds === newTrackIds && container.children.length === tracks.length) {
        console.log('Рендер пропущен - данные не изменились');
        return;
    }


    container.innerHTML = tracks.map((t) => `
        <div class="track-row" data-track-id="${t.id}" draggable="true">
            <div class="drag-handle">⠿</div>
            <div class="pos">${t.position}</div>
            <div class="info">
                <div class="name">${t.title}</div>
                <div class="artist">${(t.artists || []).map(a => a.name).join(', ')}</div>
            </div>
            ${t.audioUrl || t.audioPath ? `<audio controls src="/api/files/${encodeURI(t.audioUrl || t.audioPath)}"></audio>` : ''}
            <button class="remove-track-btn">✕</button>
        </div>
    `).join('');

    container.querySelectorAll('.track-row').forEach(row => {
        row.addEventListener('dragstart', (e) => {
            draggedTrackId = row.dataset.trackId;
            row.classList.add('dragging');
        });
        row.addEventListener('dragend', () => {
            row.classList.remove('dragging');
            container.querySelectorAll('.track-row').forEach(r => r.classList.remove('drag-over'));
            draggedTrackId = null;
        });
        row.addEventListener('dragover', (e) => {
            e.preventDefault();
            if (row.dataset.trackId !== draggedTrackId) row.classList.add('drag-over');
        });
        row.addEventListener('dragleave', () => row.classList.remove('drag-over'));
        row.addEventListener('drop', async (e) => {
            e.preventDefault();
            row.classList.remove('drag-over');
            const targetTrackId = row.dataset.trackId;
            if (!draggedTrackId || draggedTrackId === targetTrackId) return;
            const targetTrack = tracks.find(t => t.id === targetTrackId);
            if (!targetTrack) return;

            const draggedTrack = tracks.find(t => t.id === draggedTrackId);
            const oldPos = draggedTrack.position;
            const newPos = targetTrack.position;

            tracks.forEach(t => {
                if (newPos < oldPos && t.position >= newPos && t.position < oldPos) t.position++;
                else if (newPos > oldPos && t.position > oldPos && t.position <= newPos) t.position--;
            });
            draggedTrack.position = newPos;
            tracks.sort((a, b) => a.position - b.position);
            renderTracklist();

            try {
                await apiPut(`/api/releases/${releaseId}/tracks/${draggedTrackId}/position`, { position: newPos });
            } catch (e) {
                showStatus('Ошибка сохранения порядка', false);
                await loadRelease();
            }
        });
    });

    container.querySelectorAll('.remove-track-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const trackId = btn.closest('.track-row').dataset.trackId;
            if (!confirm('Убрать трек из релиза?')) return;
            try {
                await apiDelete('/api/releases/' + releaseId + '/tracks/' + trackId);
                await loadRelease();
            } catch (e) {
                showStatus(e.message, false);
            }
        });
    });
}

// ========== ДОБАВЛЕНИЕ ТРЕКА ==========
let allTracksLoaded = false;

document.getElementById('addTrackBtn')?.addEventListener('click', async () => {
    const panel = document.getElementById('trackSearchPanel');
    if (!panel) return;
    panel.style.display = 'block';
    document.getElementById('trackSearchResults').innerHTML = '';

    try {
        allTracks = await apiGet('/api/tracks/not-in-release/' + releaseId);
    } catch (e) {
        allTracks = [];
    }

    renderTrackSearchResults(allTracks);
});

function renderTrackSearchResults(tracks) {
    const container = document.getElementById('trackSearchResults');
    if (!container) return;

    if (tracks.length === 0) {
        container.innerHTML = '<div style="padding:10px;color:#888;">Нет доступных треков</div>';
        return;
    }

    container.innerHTML = tracks.map(t => `
        <div class="track-search-item" data-track-id="${t.id}">
            <div class="track-name">${t.title || 'Без названия'}</div>
            <div class="track-artist">${(t.artists || []).map(a => a.name).join(', ')}</div>
        </div>
    `).join('');

    container.querySelectorAll('.track-search-item').forEach(item => {
        item.addEventListener('click', async () => {
            try {
                await apiPost('/api/releases/' + releaseId + '/tracks/' + item.dataset.trackId, {});
                document.getElementById('trackSearchPanel').style.display = 'none';
                await loadRelease();
                showStatus('Трек добавлен');
            } catch (e) {
                showStatus(e.message, false);
            }
        });
    });
}

document.getElementById('trackSearchInput')?.addEventListener('input', () => {
    const query = document.getElementById('trackSearchInput').value.trim().toLowerCase();
    const filtered = allTracks.filter(t => t.title.toLowerCase().includes(query));
    renderTrackSearchResults(filtered);
});

document.getElementById('cancelTrackSearch')?.addEventListener('click', () => {
    const panel = document.getElementById('trackSearchPanel');
    if (panel) panel.style.display = 'none';
});

// ========== УДАЛЕНИЕ РЕЛИЗА ==========
document.getElementById('deleteReleaseBtn')?.addEventListener('click', async () => {
    if (!confirm('Удалить релиз?')) return;
    try {
        await apiDelete('/api/releases/' + releaseId);
        window.location.href = '/releases';
    } catch (e) {
        showStatus(e.message, false);
    }
});

// ========== ШТОРКА АТТАЧМЕНТОВ ==========
const drawer = document.getElementById('attachmentsDrawer');
const handle = document.getElementById('attachmentsHandle');
let isDragging = false;
let startY = 0;
let startHeight = 0;

if (handle && drawer) {
    handle.addEventListener('mousedown', (e) => {
        isDragging = true;
        startY = e.clientY;
        startHeight = drawer.offsetHeight;
        document.body.style.userSelect = 'none';
    });

    document.addEventListener('mousemove', (e) => {
        if (!isDragging) return;
        const deltaY = startY - e.clientY;
        const newHeight = Math.max(48, Math.min(window.innerHeight * 0.8, startHeight + deltaY));
        drawer.style.height = newHeight + 'px';
    });

    document.addEventListener('mouseup', () => {
        isDragging = false;
        document.body.style.userSelect = '';
    });
}

function openLightbox(src) {
    const overlay = document.createElement('div');
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.85);z-index:1000;display:flex;align-items:center;justify-content:center;';
    const img = document.createElement('img');
    img.src = src;
    img.style.maxWidth = '90%';
    img.style.maxHeight = '90%';
    overlay.appendChild(img);
    overlay.addEventListener('click', () => overlay.remove());
    document.body.appendChild(overlay);
}

function getFileType(contentType, filename) {
    const name = (filename || '').toLowerCase();
    if (contentType?.startsWith('image/') || /\.(jpg|jpeg|png|gif|webp)$/.test(name)) return 'image';
    if (contentType?.startsWith('audio/') || /\.(mp3|wav|ogg)$/.test(name)) return 'audio';
    if (contentType?.startsWith('text/') || /\.(txt|md|json|xml)$/.test(name)) return 'text';
    return 'other';
}

function renderAttachments() {
    const grid = document.getElementById('attachmentsGrid');
    if (!grid) return;

    const attachments = currentRelease?.attachments || [];
    const countEl = document.getElementById('attachmentCount');
    if (countEl) countEl.textContent = attachments.length;

    grid.innerHTML = '';

    attachments.forEach(a => {
        if (!a.s3Key) return;

        const url = '/api/files/' + encodeURI(a.s3Key);
        const name = a.originalFilename || 'Файл';
        const type = getFileType(a.contentType, name);

        const card = document.createElement('div');
        card.className = 'attachment-card';

        const removeBtn = document.createElement('span');
        removeBtn.className = 'remove-attach';
        removeBtn.textContent = '×';
        removeBtn.onclick = async () => {
            try {
                await apiDelete('/api/releases/' + releaseId + '/attachments/' + a.id);
                await loadRelease();
                showStatus('Файл удалён');
            } catch (e) {
                showStatus(e.message, false);
            }
        };
        card.appendChild(removeBtn);

        if (type === 'image') {
            const img = document.createElement('img');
            img.src = url;
            img.alt = name;
            img.onclick = () => openLightbox(url);
            card.appendChild(img);
        } else if (type === 'audio') {
            const audio = document.createElement('audio');
            audio.controls = true;
            audio.src = url;
            card.appendChild(audio);
        } else if (type === 'text') {
            const div = document.createElement('div');
            div.className = 'text-preview';
            div.textContent = 'Загрузка...';
            fetch(url).then(r => r.text()).then(t => {
                div.textContent = t.substring(0, 1000);
            });
            card.appendChild(div);
        } else {
            const aEl = document.createElement('a');
            aEl.className = 'generic-file';
            aEl.href = url;
            aEl.download = name;
            aEl.innerHTML = '<span class="file-icon">📄</span><span>' + name + '</span>';
            card.appendChild(aEl);
        }

        grid.appendChild(card);
    });
}

let uploadingAttachment = false;
document.getElementById('attachmentInput')?.addEventListener('change', async (e) => {
    if (uploadingAttachment) return;
    uploadingAttachment = true;
    const file = e.target.files[0];
    if (!file) { uploadingAttachment = false; return; }
    const formData = new FormData();
    formData.append('file', file);
    try {
        await fetch('/api/releases/' + releaseId + '/attachments', {
            method: 'POST',
            body: formData
        });
        await loadRelease();
        showStatus('Файл загружен');
        e.target.value = '';
    } catch (e) {
        showStatus('Ошибка загрузки', false);
    }
    uploadingAttachment = false;
});

// ========== ЗАПУСК ==========
document.addEventListener('DOMContentLoaded', () => {
    if (typeof releaseId !== 'undefined') {
        loadRelease();
    }
});