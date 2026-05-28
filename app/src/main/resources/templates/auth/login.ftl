<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Вход — Composer Desk</title>
    <link rel="stylesheet" href="/css/auth.css">
</head>
<body>
<div class="form-container">
    <h1>Вход</h1>

    <#if error??>
        <div class="error-message">${error}</div>
    </#if>

    <form action="/login" method="post">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <input type="text" name="username" placeholder="Имя пользователя" required autocomplete="off">
        <input type="password" name="password" placeholder="Пароль" required>
        <input type="submit" value="Войти">
    </form>

    <div class="alternative">
        Нет аккаунта? <a href="/register">Зарегистрироваться</a>
    </div>
</div>
</body>
</html>