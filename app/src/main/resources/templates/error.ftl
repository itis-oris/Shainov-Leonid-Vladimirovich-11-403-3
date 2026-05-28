<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Ошибка ${status!500} — Composer Desk</title>
    <link rel="stylesheet" href="/css/auth.css">
    <style>
        .error-container {
            text-align: center;
        }
        .error-code {
            font-size: 72px;
            font-weight: 600;
            color: #111;
            margin: 0;
        }
        .error-message {
            font-size: 16px;
            color: #888;
            margin: 12px 0 32px;
        }
    </style>
</head>
<body>
<div class="form-container error-container">
    <h1 class="error-code">${status!500}</h1>
    <p class="error-message">${message!'Что-то пошло не так'}</p>
    <a href="/home" style="color: #111;">На главную</a>
</div>
</body>
</html>