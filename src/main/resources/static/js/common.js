// Shared API calls, header and utilities used by every page

/**
 * Calls the API. On failure, throws an Error built from the server's {"status", "message"} response.
 * Sends FormData as multipart and plain objects as JSON.
 */
async function api(method, url, body) {
  const options = { method, headers: {} };
  if (body instanceof FormData) {
    options.body = body;
  } else if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  const res = await fetch(url, options);
  if (res.status === 204) {
    return null;
  }
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const error = new Error((data && data.message) || `요청 실패 (${res.status})`);
    error.status = res.status;
    throw error;
  }
  return data;
}

/** Returns the logged-in user, or null if not logged in. */
async function getMe() {
  try {
    return await api('GET', '/api/auth/me');
  } catch (e) {
    return null;
  }
}

/** Renders the page header and returns the logged-in user. */
async function renderHeader() {
  const me = await getMe();

  // Browser tab icon (duck)
  const icon = document.createElement('link');
  icon.rel = 'icon';
  icon.href = '/images/duck-o.svg';
  document.head.append(icon);

  // The duck replaces the "O" of the logo, followed by the rest of the logo text
  const header = document.createElement('header');
  header.className = 'site-header';
  header.innerHTML = `
    <div class="brand">
      <a class="logo" href="/" aria-label="OriWeb 게시판 홈"><img class="logo-o" src="/images/duck-o.svg" alt="O"><span class="logo-text">riWeb 게시판</span></a>
    </div>
    <nav></nav>`;
  const nav = header.querySelector('nav');

  if (me) {
    const name = document.createElement('span');
    name.className = 'user';
    const avatar = document.createElement('img');
    avatar.className = 'avatar';
    avatar.src = avatarOf(me);
    avatar.alt = '';
    const text = document.createElement('span');
    text.textContent = `${me.username}님`;
    name.append(avatar, text);
    const logout = document.createElement('button');
    logout.className = 'link';
    logout.textContent = 'Logout';
    logout.onclick = async () => {
      await api('POST', '/api/auth/logout');
      location.href = '/';
    };
    // Show the Admin menu to admins only (the server enforces the actual permission)
    if (isAdmin(me)) {
      const admin = document.createElement('a');
      admin.href = '/admin.html';
      admin.innerHTML = '<img class="pad" src="/images/lily-pad.svg" alt="">Admin';
      nav.append(name, admin, logout);
    } else {
      nav.append(name, logout);
    }
  } else {
    nav.innerHTML = `
      <a href="/login.html"><img class="pad" src="/images/lily-pad.svg" alt="">Login</a>
      <a href="/signup.html"><img class="pad" src="/images/lily-pad.svg" alt="">Join</a>`;
  }

  // Place the header inside the board width (top of main)
  document.querySelector('main').prepend(header);
  return me;
}

// "The Ugly Duckling" motif: regular users are geese, admins are the hacker duck
const AVATARS = ['goose-relax', 'goose-write', 'goose-coffee', 'goose-read', 'goose-wave', 'goose-walk'];

/**
 * Profile picture: the hacker duck for admins, one of the geese for regular users.
 * Picked by user ID so the same user always gets the same goose.
 */
function isAdmin(user) {
  return !!user && user.role === 'ADMIN';
}

function avatarOf(user) {
  if (isAdmin(user)) {
    return '/images/hacker_duck.png';
  }
  return `/images/avatars/${AVATARS[user.id % AVATARS.length]}.svg`;
}

/** Call on pages that require login. Redirects to the login page if not logged in. */
function requireLogin(me) {
  if (!me) {
    alert('로그인이 필요합니다.');
    location.href = `/login.html?redirect=${encodeURIComponent(location.pathname + location.search)}`;
    return false;
  }
  return true;
}

function getParam(name) {
  return new URLSearchParams(location.search).get(name);
}

function formatDate(value) {
  return value ? value.replace('T', ' ').substring(0, 16) : '';
}

/** Converts a number to Roman numerals (1-3999). Returns it unchanged if out of range. e.g. 9 -> IX */
function toRoman(num) {
  if (!Number.isInteger(num) || num < 1 || num > 3999) return String(num);
  const table = [
    [1000, 'M'], [900, 'CM'], [500, 'D'], [400, 'CD'], [100, 'C'], [90, 'XC'],
    [50, 'L'], [40, 'XL'], [10, 'X'], [9, 'IX'], [5, 'V'], [4, 'IV'], [1, 'I'],
  ];
  let result = '';
  for (const [value, symbol] of table) {
    while (num >= value) {
      result += symbol;
      num -= value;
    }
  }
  return result;
}

/** updatedAt differs slightly from createdAt even right after creation, so only treat 1s+ differences as edits. */
function isEdited(item) {
  return new Date(item.updatedAt) - new Date(item.createdAt) > 1000;
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
