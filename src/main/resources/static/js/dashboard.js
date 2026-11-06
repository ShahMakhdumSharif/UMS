// Shared Dashboard Logic

function getAuth(expectedRole = null) {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token || !userStr) {
        window.location.href = '/index.html';
        return null;
    }

    try {
        const user = JSON.parse(userStr);
        if (expectedRole && user.role !== expectedRole) {
            window.location.href = '/ums/dashboard';
            return null;
        }
        return { token, user };
    } catch (e) {
        localStorage.clear();
        window.location.href = '/index.html';
        return null;
    }
}

function canCreateItem(resource, user) {
    if (!user) return false;
    const role = user.role;
    if (role === 'ROLE_ADMIN') return true;
    if (role === 'ROLE_TEACHER' && resource === 'students') return true;
    return false;
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/index.html';
}

async function apiRequest(endpoint, method = 'GET', body = null) {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(endpoint, {
        method,
        headers,
        body: body ? JSON.stringify(body) : null
    });

    if (res.status === 401) {
        logout();
        throw new Error('Session expired. Please log in again.');
    }

    let data;
    try {
        data = await res.json();
    } catch (e) {
        data = null;
    }

    if (!res.ok) {
        throw new Error((data && data.message) || `HTTP Error ${res.status}`);
    }

    return data;
}

function canModifyItem(resource, item, user) {
    if (!user || !item) return false;
    const role = user.role;
    const currentUsername = user.username;

    // Admin on Admins can only modify self
    if (role === 'ROLE_ADMIN' && resource === 'admins') {
        return item.username === currentUsername;
    }
    // Admin on Teachers and Students can manage all
    if (role === 'ROLE_ADMIN') {
        return true;
    }

    // Teacher on Students can manage all students
    if (role === 'ROLE_TEACHER' && resource === 'students') {
        return true;
    }
    // Teacher on Teachers can only modify self
    if (role === 'ROLE_TEACHER' && resource === 'teachers') {
        return item.username === currentUsername;
    }

    // Student on Students can only modify self
    if (role === 'ROLE_STUDENT' && resource === 'students') {
        return item.username === currentUsername;
    }

    return false;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
