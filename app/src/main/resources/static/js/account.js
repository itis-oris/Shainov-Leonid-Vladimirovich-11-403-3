const statusDiv = document.getElementById('status');

function showStatus(message, isSuccess = true) {
    if (!statusDiv) return;
    statusDiv.textContent = message;
    statusDiv.className = isSuccess ? 'success' : 'error';
    statusDiv.style.display = 'block';
    setTimeout(() => { statusDiv.style.display = 'none'; }, 2000);
}

async function loadAccount() {
    try {
        const data = await apiGet('/api/account');
        document.getElementById('username').textContent = data.username;
        document.getElementById('role').textContent = data.role;
    } catch (err) {
        showStatus('Ошибка загрузки', false);
    }
}

// Сменить логин
const usernameModal = document.getElementById('usernameModal');
document.getElementById('changeUsernameBtn')?.addEventListener('click', () => {
    usernameModal.style.display = 'flex';
});
document.getElementById('cancelUsername')?.addEventListener('click', () => {
    usernameModal.style.display = 'none';
});
document.getElementById('saveUsername')?.addEventListener('click', async () => {
    const newUsername = document.getElementById('newUsername').value.trim();
    if (!newUsername) return;
    try {
        await apiPut('/api/account/username', { newUsername });
        usernameModal.style.display = 'none';
        showStatus('Логин изменён', true);
        loadAccount();
    } catch (err) {
        showStatus(err.message, false);
    }
});

// Сменить пароль
const passwordModal = document.getElementById('passwordModal');
document.getElementById('changePasswordBtn')?.addEventListener('click', () => {
    passwordModal.style.display = 'flex';
});
document.getElementById('cancelPassword')?.addEventListener('click', () => {
    passwordModal.style.display = 'none';
});
document.getElementById('savePassword')?.addEventListener('click', async () => {
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    if (!oldPassword || !newPassword) return;
    try {
        await apiPut('/api/account/password', { oldPassword, newPassword });
        passwordModal.style.display = 'none';
        showStatus('Пароль изменён', true);
    } catch (err) {
        showStatus(err.message, false);
    }
});

// Удалить аккаунт
document.getElementById('deleteAccountBtn')?.addEventListener('click', async () => {
    if (!confirm('Вы точно хотите удалить аккаунт без возможности восстановления?')) return;
    try {
        await apiDelete('/api/account');
        window.location.href = '/login';
    } catch (err) {
        showStatus(err.message, false);
    }
});

loadAccount();