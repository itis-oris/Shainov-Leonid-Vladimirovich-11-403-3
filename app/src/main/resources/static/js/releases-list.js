const releasesGrid = document.getElementById('releasesGrid');
const sortSelect = document.getElementById('sortSelect');
const searchInput = document.getElementById('searchInput');
const typeFilter = document.getElementById('typeFilter');

let currentSort = 'id';
let currentSearch = '';
let currentTypes = [];

async function loadReleases() {
    try {
        const filterBody = {
            search: currentSearch || null,
            types: currentTypes.length > 0 ? currentTypes : null,
            sortBy: currentSort,
            sortDirection: currentSort === 'title' ? 'asc' : 'desc'
        };

        const releases = await apiPost('/api/releases/filter', filterBody);

        releasesGrid.querySelectorAll('.release-card:not(.new-release)').forEach(c => c.remove());

        releases.forEach(release => {
            const card = document.createElement('div');
            card.className = 'release-card';
            card.dataset.releaseId = release.id;

            const typeNames = { SINGLE: 'Сингл', EP: 'EP', ALBUM: 'Альбом' };

            card.innerHTML = `
                <div class="cover-container">
                    ${release.coverUrl
                ? `<img src="/api/files/${encodeURI(release.coverUrl)}" class="cover-image">`
                : `<div class="cover-placeholder">Нет обложки</div>`}
                    <span class="release-type">${typeNames[release.type] || release.type}</span>
                    <button class="delete-release-btn">🗑</button>
                </div>
                <div class="release-info">
                    <div class="release-title">${release.title || 'Без названия'}</div>
                    <div class="release-artists">${release.artistNames || ''}</div>
                    <div class="release-meta">${release.trackCount} треков</div>
                </div>
            `;
            releasesGrid.appendChild(card);
        });
    } catch (e) {
        console.error(e);
        showStatus('Ошибка загрузки релизов', false);
    }
}

sortSelect.addEventListener('change', () => {
    currentSort = sortSelect.value;
    loadReleases();
});

searchInput.addEventListener('input', () => {
    currentSearch = searchInput.value.trim();
    loadReleases();
});

typeFilter.addEventListener('change', () => {
    currentTypes = typeFilter.value ? [typeFilter.value] : [];
    loadReleases();
});

releasesGrid?.addEventListener('click', async (evt) => {
    const card = evt.target.closest('.release-card');
    if (!card) return;

    if (evt.target.closest('.delete-release-btn')) {
        evt.stopPropagation();
        const releaseId = card.dataset.releaseId;
        if (!confirm('Удалить релиз?')) return;
        try {
            await apiDelete('/api/releases/' + releaseId);
            card.remove();
            showStatus('Релиз удалён');
        } catch (e) {
            showStatus(e.message, false);
        }
        return;
    }

    if (!card.classList.contains('new-release')) {
        window.location.href = '/releases/' + card.dataset.releaseId;
    }
});

document.getElementById('newReleaseCard')?.addEventListener('click', async () => {
    try {
        const release = await apiPost('/api/releases', {});
        window.location.href = '/releases/' + release.id;
    } catch (e) {
        showStatus('Ошибка создания релиза', false);
    }
});

function showStatus(message, isSuccess = true) {
    const statusDiv = document.getElementById('status');
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

loadReleases();