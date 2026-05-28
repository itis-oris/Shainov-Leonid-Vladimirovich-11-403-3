<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Мои треки — Composer Desk</title>
    <link rel="stylesheet" href="/css/tracks-list.css">

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
        <section class="top-panel">
            <input type="text" id="searchInput" placeholder="Поиск по названию">
            <select id="sortSelect">
                <option value="updatedDate" selected>Сначала новые</option>
                <option value="title">По алфавиту</option>
                <option value="createdDate">По дате создания</option>
            </select>

            <div class="tags-filter">
                <select id="tagSelect">
                    <option value="">Выберите тег</option>
                </select>
                <div id="selectedTags"></div>
            </div>
        </section>

        <section class="tracks-grid" id="tracksGrid">
            <div class="track-card new-track" id="newTrackCard">
                <div class="cover-container">
                    <div class="cover-placeholder">＋</div>
                </div>
                <div class="track-info">
                    <div class="track-title">Новый трек</div>
                    <div class="audio-placeholder small">Создать</div>
                </div>
            </div>
        </section>
    </main>
</div>

<div id="status"></div>

<script src="/js/api.js"></script>
<script src="/js/tracks-list.js"></script>

</body>
</html>