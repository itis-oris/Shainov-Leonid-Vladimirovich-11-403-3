async function apiGet(url) {
    const response = await fetch(url);
    if (response.status === 401) {
        window.location.href = '/login';
        throw new Error('Unauthorized');
    }
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Ошибка');
    }
    return response.json();
}

async function apiPost(url, body) {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (response.status === 401) {
        window.location.href = '/login';
        throw new Error('Unauthorized');
    }
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Ошибка');
    }
    return response.json();
}

async function apiPut(url, body) {
    const response = await fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (response.status === 401) {
        window.location.href = '/login';
        throw new Error('Unauthorized');
    }
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Ошибка');
    }
    return response.json();
}

async function apiDelete(url) {
    const response = await fetch(url, { method: 'DELETE' });
    if (response.status === 401) {
        window.location.href = '/login';
        throw new Error('Unauthorized');
    }
    if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || 'Ошибка');
    }
    return response.json();
}