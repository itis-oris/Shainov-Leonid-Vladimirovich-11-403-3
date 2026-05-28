<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование трека</title>
    <link rel="stylesheet" href="/css/track-edit.css">

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
            <div class="media-block">
                <div class="cover-section">
                    <div class="cover-container" id="coverContainer">
                        <div class="cover-placeholder">Нет обложки</div>
                    </div>
                    <input type="file" id="imageInput" class="file-input" accept="image/*">
                    <label for="imageInput" class="file-button">Изменить обложку</label>
                </div>

                <div class="audio-section" id="audioContainer">
                    <div class="audio-placeholder">Нет аудио</div>
                </div>
                <input type="file" id="audioInput" class="file-input" accept="audio/*">
                <label for="audioInput" class="file-button">Добавить аудио</label>
            </div>
        </section>

        <section class="center-panel">
            <div class="track-header">
                <input type="text" id="trackTitle" class="track-title" placeholder="Название трека">
                <button id="deleteTrackBtn" class="delete-track-btn">🗑️</button>
            </div>
            <div id="titleFeedback" class="title-feedback"></div>

            <label>Исполнители</label>
            <div id="selectedArtists" class="selected-artists"></div>
            <div class="artist-search">
                <input type="text" id="artistSearchInput" placeholder="Поиск исполнителей..." autocomplete="off">
                <div id="artistDropdown" class="artist-dropdown" style="display:none;"></div>
            </div>

            <label>Теги</label>
            <div id="tagsContainer">
                <div class="tag add-tag" id="addTagBtn">+</div>
            </div>

            <label for="description">Описание</label>
            <input id="description" class="description-input" placeholder="Описание трека">

            <label for="lyrics">Текст</label>
            <div class="lyrics-wrapper">
                <textarea id="lyrics" class="lyrics-input" placeholder="Текст песни..."></textarea>
                <button id="rhymeBtn" class="rhyme-btn" title="Найти рифму к выделенному слову">🔤 Рифмы</button>
                <div id="rhymePanel" class="rhyme-panel" style="display:none;"></div>
            </div>
        </section>

        <aside class="right-panel">
            <h3>Ваши треки</h3>
            <div id="tracksList" class="tracks-list"></div>
        </aside>
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

<script>const trackId = '${trackId}';</script>
<script src="/js/api.js"></script>
<script src="/js/track-edit.js"></script>

</body>
</html>