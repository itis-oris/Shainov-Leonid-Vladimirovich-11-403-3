<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование релиза — Composer Desk</title>
    <link rel="stylesheet" href="/css/release-edit.css">

</head>
<body>

<div class="page-wrapper">
    <nav class="sidebar-nav">
        <button onclick="location.href='/home'">🏠</button>
        <button onclick="location.href='/tracks'">🎵</button>
        <button onclick="location.href='/artists'">🎤</button>
        <button onclick="location.href='/releases'">💿</button>
        <button onclick="location.href='/account'">👤</button>
    </nav>

    <main class="content-wrapper">
        <section class="left-panel">
            <div class="cover-section">
                <div class="cover-container" id="coverContainer">
                    <div class="cover-placeholder">Нет обложки</div>
                </div>
                <input type="file" id="coverInput" class="file-input" accept="image/*">
                <label for="coverInput" class="file-button">Изменить обложку</label>
            </div>

            <div class="track-header">
                <input type="text" id="releaseTitle" class="release-title-input" placeholder="Название релиза">
                <button id="deleteReleaseBtn" class="delete-btn">🗑️</button>
            </div>
            <div id="titleFeedback" class="title-feedback"></div>

            <select id="typeSelect">
                <option value="SINGLE">Сингл</option>
                <option value="EP">EP</option>
                <option value="ALBUM">Альбом</option>
            </select>

            <label>Исполнители</label>
            <div id="selectedArtists" class="selected-artists"></div>
            <div class="artist-search">
                <input type="text" id="artistSearchInput" placeholder="Поиск исполнителей..." autocomplete="off">
                <div id="artistDropdown" class="artist-dropdown" style="display:none;"></div>
            </div>

            <label for="descriptionInput">Описание</label>
            <textarea id="descriptionInput" placeholder="Описание релиза"></textarea>
        </section>

        <section class="right-panel">
            <div class="tracklist-header">
                <h3>Треклист</h3>
                <button id="addTrackBtn" class="add-track-btn">+ Добавить трек</button>
            </div>

            <div id="trackSearchPanel" class="track-search-panel" style="display:none;">
                <input type="text" id="trackSearchInput" placeholder="Поиск треков...">
                <div id="trackSearchResults" class="track-search-results"></div>
                <button id="cancelTrackSearch" class="cancel-btn">Отмена</button>
            </div>

            <div id="tracklist" class="tracklist"></div>
        </section>
    </main>
</div>

<div id="attachmentsDrawer" class="attachments-drawer">
    <div id="attachmentsHandle" class="attachments-handle">
        <span>📎 Файлы</span>
        <span id="attachmentCount">0</span>
    </div>
    <div id="attachmentsContent" class="attachments-content">
        <div id="attachmentsGrid" class="attachments-grid"></div>
        <input type="file" id="attachmentInput" class="file-input" multiple>
        <label for="attachmentInput" class="add-attachment-btn">+ Добавить файл</label>
    </div>
</div>

<div id="status"></div>

<script>const releaseId = '${releaseId}';</script>
<script src="/js/api.js"></script>
<script src="/js/release-edit.js"></script>

</body>
</html>