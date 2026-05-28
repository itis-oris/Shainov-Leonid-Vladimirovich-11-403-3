<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Исполнители — Composer Desk</title>
    <link rel="stylesheet" href="/css/artists-list.css">

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
        <div class="header">
            <h2>Исполнители</h2>
            <button id="addArtistBtn" class="add-btn">+ Добавить</button>
        </div>

        <input type="text" id="searchInput" placeholder="Поиск по имени">

        <div id="artistsList" class="artists-list"></div>
    </main>
</div>

<div id="status"></div>

<script src="/js/api.js"></script>
<script src="/js/artists-list.js"></script>

</body>
</html>