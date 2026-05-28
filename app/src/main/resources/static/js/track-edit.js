const statusDiv = document.getElementById('status');
let currentTrack = null;

function showStatus(message, isSuccess = true) {
    if (!statusDiv) return;
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

// ========== ЗАГРУЗКА ДАННЫХ ТРЕКА ==========
async function loadTrack() {
    try {
        currentTrack = await apiGet('/api/tracks/' + trackId);
        renderTrack(currentTrack);
        loadArtists();
        loadTracksList();
        loadAttachments();
    } catch (e) {
        showStatus('Ошибка загрузки трека', false);
    }
}

function renderTrack(track) {
    document.getElementById('trackTitle').value = track.title || '';
    document.getElementById('description').value = track.description || '';
    document.getElementById('lyrics').value = track.lyrics || '';

    const coverContainer = document.getElementById('coverContainer');
    if (track.coverUrl) {
        coverContainer.innerHTML = `<img src="/api/files/${encodeURI(track.coverUrl)}" class="cover-image" id="preview-image" onerror="this.parentElement.innerHTML='<div class=\\'cover-placeholder\\'>Ошибка загрузки</div>'">`;
    } else {
        coverContainer.innerHTML = '<div class="cover-placeholder">Нет обложки</div>';
    }

    const audioContainer = document.getElementById('audioContainer');
    if (track.audioUrl) {
        audioContainer.innerHTML = `<audio controls id="preview-audio" class="audio-player" src="/api/files/${encodeURI(track.audioUrl)}"></audio>`;
    } else {
        audioContainer.innerHTML = '<div class="audio-placeholder">Нет аудио</div>';
    }

    const tagsContainer = document.getElementById('tagsContainer');
    tagsContainer.innerHTML = '<div class="tag add-tag" id="addTagBtn">+</div>';
    (track.tags || []).forEach(tag => {
        const tagEl = document.createElement('div');
        tagEl.className = 'tag';
        tagEl.textContent = tag.name;
        tagEl.style.backgroundColor = tag.color || '#999';
        tagEl.dataset.tagId = tag.id;
        tagEl.addEventListener('click', () => removeTag(tag.id, tagEl));
        tagsContainer.appendChild(tagEl);
    });
    document.getElementById('addTagBtn').addEventListener('click', addTagPrompt);
}

// ========== ИСПОЛНИТЕЛИ ==========
let allArtists = [];
let selectedArtistIds = [];

async function loadArtists() {
    try {
        allArtists = await apiGet('/api/artists');
        selectedArtistIds = (currentTrack.artists || []).map(a => a.id);
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
        const data = await apiPut('/api/tracks/' + trackId + '/artists', { artistIds: selectedArtistIds });
        currentTrack.artists = data.artists;
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


// ========== АВТОСОХРАНЕНИЕ ПОЛЕЙ ==========
['trackTitle', 'description', 'lyrics'].forEach(id => {
    document.getElementById(id)?.addEventListener('blur', async () => {
        const title = document.getElementById('trackTitle').value;
        const description = document.getElementById('description').value;
        const lyrics = document.getElementById('lyrics').value;
        try {
            const data = await apiPut('/api/tracks/' + trackId, { title, description, lyrics });
            currentTrack = data;
            showStatus('Сохранено');
        } catch (e) {
            showStatus(e.message, false);
        }
    });
});

// ========== DISCOGS ==========
let titleTimeout;
document.getElementById('trackTitle')?.addEventListener('input', () => {
    clearTimeout(titleTimeout);
    titleTimeout = setTimeout(async () => {
        const title = document.getElementById('trackTitle').value.trim();
        if (!title) return;
        try {
            const data = await apiGet('/api/discogs/check-track?title=' + encodeURIComponent(title));
            const feedback = document.getElementById('titleFeedback');
            feedback.textContent = data.warning;
            feedback.style.display = 'block';
            setTimeout(() => { feedback.style.display = 'none'; }, 3000);
        } catch (e) {}
    }, 600);
});

// ========== РИФМЫ ==========
document.getElementById('rhymeBtn')?.addEventListener('click', async () => {
    const textarea = document.getElementById('lyrics');
    const selected = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd).trim();
    if (!selected) {
        showStatus('Выделите слово в тексте', false);
        return;
    }
    try {
        const data = await apiGet('/api/rhymes?word=' + encodeURIComponent(selected));
        const panel = document.getElementById('rhymePanel');
        panel.innerHTML = data.rhymes.map(r => `<div class="rhyme-word" data-word="${r}">${r}</div>`).join('');
        panel.style.display = 'block';

        panel.querySelectorAll('.rhyme-word').forEach(el => {
            el.addEventListener('click', () => {
                const start = textarea.selectionStart;
                const end = textarea.selectionEnd;
                textarea.value = textarea.value.substring(0, start) + el.dataset.word + textarea.value.substring(end);
                panel.style.display = 'none';
            });
        });
    } catch (e) {
        showStatus('Ошибка загрузки рифм', false);
    }
});

// ========== ТЕГИ ==========
function addTagPrompt() {
    const container = document.getElementById('tagsContainer');
    if (!container) return;
    if (container.querySelector('.new-tag-input')) return;

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = 'Название тега';
    input.className = 'new-tag-input';
    input.style.cssText = 'width:120px; padding:4px 8px; border-radius:12px; border:1px solid #ccc; font-size:14px;';

    const addBtn = document.getElementById('addTagBtn');
    if (!addBtn) return;
    container.insertBefore(input, addBtn);
    input.focus();

    let submitted = false;

    const submitTag = async () => {
        if (submitted) return;
        submitted = true;

        const name = input.value.trim();
        if (!name) {
            input.remove();
            return;
        }

        const color = '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0');

        try {
            const res = await fetch('/api/tracks/' + trackId + '/tags', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, color })
            });

            if (!res.ok) {
                const err = await res.json();
                showStatus('Ошибка: ' + (err.message || 'неизвестная'), false);
                input.remove();
                return;
            }

            const data = await res.json();
            currentTrack = data;

            input.remove();
            const newTag = document.createElement('div');
            newTag.className = 'tag';
            newTag.textContent = name;
            newTag.style.backgroundColor = color;
            const lastTag = (data.tags || []).slice(-1)[0];
            if (lastTag) {
                newTag.dataset.tagId = lastTag.id;
                newTag.addEventListener('click', () => removeTag(lastTag.id, newTag));
            }
            const addBtn2 = document.getElementById('addTagBtn');
            if (addBtn2) {
                container.insertBefore(newTag, addBtn2);
            } else {
                container.appendChild(newTag);
            }
            showStatus('Тег добавлен');
        } catch (e) {
            input.remove();
            showStatus('Ошибка сети', false);
        }
    };

    input.addEventListener('keydown', (ev) => {
        if (ev.key === 'Enter') {
            ev.preventDefault();
            submitTag();
        }
    });

    input.addEventListener('blur', () => {
        setTimeout(submitTag, 200);
    });
}

async function removeTag(tagId, element) {
    try {
        const data = await apiDelete('/api/tracks/' + trackId + '/tags/' + tagId);
        currentTrack = data;
        element.remove();
        showStatus('Тег удалён');
    } catch (e) {
        showStatus(e.message, false);
    }
}

// ========== ОБЛОЖКА ==========
document.getElementById('imageInput')?.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const coverContainer = document.getElementById('coverContainer');
    coverContainer.innerHTML = `<img src="${URL.createObjectURL(file)}" class="cover-image" id="preview-image">`;

    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch('/api/tracks/' + trackId + '/cover', {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        currentTrack = data;
        coverContainer.innerHTML = `<img src="/api/files/${encodeURI(data.coverUrl)}" class="cover-image" id="preview-image" onerror="this.parentElement.innerHTML='<div class=\\'cover-placeholder\\'>Ошибка загрузки</div>'">`;
        showStatus('Обложка обновлена');
    } catch (e) {
        showStatus('Ошибка загрузки обложки', false);
    }
});

// ========== АУДИО ==========
document.getElementById('audioInput')?.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const audioContainer = document.getElementById('audioContainer');
    audioContainer.innerHTML = `<audio controls id="preview-audio" class="audio-player" src="${URL.createObjectURL(file)}"></audio>`;

    const formData = new FormData();
    formData.append('file', file);

    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 120000); // 2 минуты

        const res = await fetch('/api/tracks/' + trackId + '/audio', {
            method: 'POST',
            body: formData,
            signal: controller.signal
        });

        clearTimeout(timeoutId);

        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || 'Upload failed');
        }

        const data = await res.json();
        currentTrack = data;
        audioContainer.innerHTML = `<audio controls id="preview-audio" class="audio-player" src="/api/files/${encodeURI(data.audioUrl)}"></audio>`;
        showStatus('Аудио обновлено');
    } catch (e) {
        if (e.name === 'AbortError') {
            showStatus('Загрузка прервана: файл слишком большой', false);
        } else {
            showStatus('Ошибка загрузки аудио', false);
        }
    }
});

// ========== УДАЛЕНИЕ ТРЕКА ==========
document.getElementById('deleteTrackBtn')?.addEventListener('click', async () => {
    if (!confirm('Удалить трек безвозвратно?')) return;
    try {
        await apiDelete('/api/tracks/' + trackId);
        window.location.href = '/tracks';
    } catch (e) {
        showStatus(e.message, false);
    }
});

// ========== СПИСОК ТРЕКОВ СПРАВА ==========
async function loadTracksList() {
    try {
        const tracks = await apiGet('/api/tracks?excludeId=' + trackId);
        const container = document.getElementById('tracksList');
        container.innerHTML = tracks.map(t => `
            <div class="track-item" onclick="location.href='/tracks/${t.id}'">
                <div class="track-cover">
                    ${t.coverUrl ? `<img src="/api/files/${encodeURI(t.coverUrl)}" alt="">` : '<div class="cover-placeholder small"></div>'}
                </div>
                <div class="track-name">${t.title}<br><small>${(t.artists || []).map(a => a.name).join(', ')}</small></div>
            </div>
        `).join('');
    } catch (e) {}
}

// ========== АТТАЧМЕНТЫ ==========
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

async function loadAttachments() {
    const grid = document.getElementById('attachmentsGrid');
    if (!grid) return;

    const attachments = currentTrack?.attachments || [];
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
                await apiDelete('/api/tracks/' + trackId + '/attachments/' + a.id);
                await loadTrack();
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
        await fetch('/api/tracks/' + trackId + '/attachments', {
            method: 'POST',
            body: formData
        });
        await loadTrack();
        showStatus('Файл загружен');
        e.target.value = '';
    } catch (e) {
        showStatus('Ошибка загрузки', false);
    }
    uploadingAttachment = false;
});

loadTrack();