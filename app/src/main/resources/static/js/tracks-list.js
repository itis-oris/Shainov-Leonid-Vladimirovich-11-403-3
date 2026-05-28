const tracksGrid = document.getElementById('tracksGrid');
const sortSelect = document.getElementById('sortSelect');
const searchInput = document.getElementById('searchInput');
const tagSelect = document.getElementById('tagSelect');
const selectedTagsContainer = document.getElementById('selectedTags');

let currentSort = 'updatedDate';
let currentSearch = '';
let selectedTags = [];
let allTags = [];

async function loadTags() {
    try {
        const tracks = await apiGet('/api/tracks');
        const tagSet = new Map();
        tracks.forEach(t => {
            (t.tags || []).forEach(tag => {
                if (!tagSet.has(tag.id)) tagSet.set(tag.id, tag);
            });
        });
        allTags = Array.from(tagSet.values());
        const select = document.getElementById('tagSelect');
        allTags.forEach(tag => {
            const option = document.createElement('option');
            option.value = tag.id;
            option.textContent = tag.name;
            option.dataset.color = tag.color || '#888';
            select.appendChild(option);
        });
    } catch (e) {}
}

sortSelect.addEventListener('change', () => {
    currentSort = sortSelect.value;
    loadTracks();
});

searchInput.addEventListener('input', () => {
    currentSearch = searchInput.value.trim();
    loadTracks();
});

tagSelect?.addEventListener('change', () => {
    const option = tagSelect.selectedOptions[0];
    if (!option.value) return;
    const tagObj = {
        id: option.value,
        name: option.textContent,
        color: option.dataset.color || '#888'
    };
    if (!selectedTags.find(t => t.id === tagObj.id)) {
        selectedTags.push(tagObj);
        renderSelectedTags();
        loadTracks();
    }
    tagSelect.value = '';
});

function renderSelectedTags() {
    selectedTagsContainer.innerHTML = '';
    selectedTags.forEach(tagObj => {
        const pill = document.createElement('div');
        pill.className = 'tag-pill';
        pill.textContent = tagObj.name;
        pill.style.backgroundColor = tagObj.color;
        pill.addEventListener('click', () => {
            selectedTags = selectedTags.filter(t => t.id !== tagObj.id);
            renderSelectedTags();
            loadTracks();
        });
        selectedTagsContainer.appendChild(pill);
    });
}

async function loadTracks() {
    try {
        const filterBody = {
            search: currentSearch || null,
            tagIds: selectedTags.length > 0 ? selectedTags.map(t => t.id) : null,
            sortBy: currentSort,
            sortDirection: currentSort === 'title' ? 'asc' : 'desc'
        };

        console.log('Filter body:', filterBody);

        const tracks = await apiPost('/api/tracks/filter', filterBody);
        console.log('Tracks loaded:', tracks.length);

        tracksGrid.querySelectorAll('.track-card:not(.new-track)').forEach(c => c.remove());

        tracks.forEach(track => {
            const card = document.createElement('div');
            card.className = 'track-card';
            card.dataset.trackId = track.id;

            card.innerHTML = `
                <div class="cover-container">
                    ${track.coverUrl
                ? `<img src="/api/files/${encodeURI(track.coverUrl)}" class="cover-image">`
                : `<div class="cover-placeholder">Нет обложки</div>`}
                    <button class="delete-track-btn">🗑</button>
                </div>
                <div class="track-info">
                    <div class="track-title">${track.title || 'Без названия'}</div>
                    <div class="artist-names">${(track.artists || []).map(a => a.name).join(', ') || '&nbsp;'}</div>
                    ${track.audioUrl
                ? `<audio controls src="/api/files/${encodeURI(track.audioUrl)}"></audio>`
                : `<div class="audio-placeholder small">Нет аудио</div>`}
                </div>
            `;
            tracksGrid.appendChild(card);
        });
    } catch (e) {
        console.error(e);
        showStatus('Ошибка загрузки треков', false);
    }
}

tracksGrid?.addEventListener('click', async (evt) => {
    const card = evt.target.closest('.track-card');
    if (!card) return;

    if (evt.target.closest('.delete-track-btn')) {
        evt.stopPropagation();
        const trackId = card.dataset.trackId;
        if (!confirm('Удалить трек безвозвратно?')) return;
        try {
            await apiDelete('/api/tracks/' + trackId);
            card.remove();
            showStatus('Трек удалён');
        } catch (e) {
            showStatus(e.message, false);
        }
        return;
    }

    if (!card.classList.contains('new-track')) {
        window.location.href = '/tracks/' + card.dataset.trackId;
    }
});

async function createTrackAndGo() {
    try {
        const track = await apiPost('/api/tracks', {});
        window.location.href = '/tracks/' + track.id;
    } catch (e) {
        showStatus('Ошибка создания трека', false);
    }
}

function showStatus(message, isSuccess = true) {
    const statusDiv = document.getElementById('status');
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

document.getElementById('newTrackCard')?.addEventListener('click', createTrackAndGo);

loadTags();
loadTracks();