<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Аккаунт — Composer Desk</title>
    <link rel="stylesheet" href="/css/account.css">
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
        <div class="account-wrapper">
            <h1>Привет, <span id="username">...</span>!</h1>
            <p>Роль: <span id="role">...</span></p>

            <button id="changeUsernameBtn">Сменить логин</button>
            <button id="changePasswordBtn">Сменить пароль</button>
            <button id="deleteAccountBtn" class="danger-btn">Удалить аккаунт</button>

            <form action="/logout" method="post" style="margin-top:16px;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="logout-btn">Выйти</button>
            </form>
        </div>
    </main>
</div>

<div id="status"></div>

<div id="usernameModal" class="modal" style="display: none;">
    <div class="modal-content">
        <h3>Сменить логин</h3>
        <input type="text" id="newUsername" placeholder="Новый логин">
        <button id="saveUsername">Сохранить</button>
        <button id="cancelUsername">Отмена</button>
    </div>
</div>

<div id="passwordModal" class="modal" style="display: none;">
    <div class="modal-content">
        <h3>Сменить пароль</h3>
        <input type="password" id="oldPassword" placeholder="Старый пароль">
        <input type="password" id="newPassword" placeholder="Новый пароль">
        <button id="savePassword">Сохранить</button>
        <button id="cancelPassword">Отмена</button>
    </div>
</div>

<script src="/js/api.js"></script>
<script src="/js/account.js"></script>
</body>
</html>