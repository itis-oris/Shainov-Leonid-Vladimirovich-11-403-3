<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Главная — Composer Desk</title>
    <link rel="stylesheet" href="/css/home.css">
</head>
<body>

<div class="home-wrapper">
    <h1 class="home-title">Composer Desk</h1>
    <p class="home-subtitle">Рабочий стол композитора</p>

    <div class="home-grid">
        <a href="/tracks" class="home-card">
            <span class="home-icon">🎵</span>
            <span class="home-label">Треки</span>
        </a>
        <a href="/releases" class="home-card">
            <span class="home-icon">💿</span>
            <span class="home-label">Релизы</span>
        </a>
        <a href="/artists" class="home-card">
            <span class="home-icon">🎤</span>
            <span class="home-label">Исполнители</span>
        </a>
        <a href="/account" class="home-card">
            <span class="home-icon">👤</span>
            <span class="home-label">Аккаунт</span>
        </a>
    </div>

    <form action="/logout" method="post" class="home-logout">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <button type="submit" class="logout-link">Выйти</button>
    </form>
</div>

</body>
</html>