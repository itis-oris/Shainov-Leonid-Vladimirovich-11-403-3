<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Релизы — Composer Desk</title>
    <link rel="stylesheet" href="/css/releases-list.css">

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
                <option value="id" selected>Сначала новые</option>
                <option value="title">По алфавиту</option>
            </select>
            <select id="typeFilter">
                <option value="">Все типы</option>
                <option value="SINGLE">Синглы</option>
                <option value="EP">EP</option>
                <option value="ALBUM">Альбомы</option>
            </select>
        </section>

        <section class="releases-grid" id="releasesGrid">
            <div class="release-card new-release" id="newReleaseCard">
                <div class="cover-container">
                    <div class="cover-placeholder">＋</div>
                </div>
                <div class="release-info">
                    <div class="release-title">Новый релиз</div>
                    <div class="release-meta">Создать</div>
                </div>
            </div>
        </section>
    </main>
</div>

<div id="status"></div>

<script src="/js/api.js"></script>
<script src="/js/releases-list.js"></script>

</body>
</html>