// 모든 페이지에서 공통으로 쓰는 API 호출, 헤더, 유틸 함수

/**
 * API 호출. 실패하면 서버의 {"status", "message"} 응답으로 Error를 던진다.
 * body가 FormData면 multipart로, 객체면 JSON으로 보낸다.
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

/** 로그인 사용자 조회. 로그인하지 않았으면 null. */
async function getMe() {
  try {
    return await api('GET', '/api/auth/me');
  } catch (e) {
    return null;
  }
}

/** 상단 헤더를 그리고 로그인 사용자를 반환한다. */
async function renderHeader() {
  const me = await getMe();

  // 브라우저 탭 아이콘 (오리 얼굴)
  const icon = document.createElement('link');
  icon.rel = 'icon';
  icon.href = '/images/duck-o.svg';
  document.head.append(icon);

  // 로고의 O 자리에 오리, 그 뒤를 "riWeb 게시판" 글자가 따라감
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
    nav.append(name, logout);
  } else {
    nav.innerHTML = `
      <a href="/login.html"><img class="pad" src="/images/lily-pad.svg" alt="">Login</a>
      <a href="/signup.html"><img class="pad" src="/images/lily-pad.svg" alt="">Join</a>`;
  }

  // 게시판과 같은 폭 안(main 맨 위)에 배치
  document.querySelector('main').prepend(header);
  return me;
}

// 미운 오리 새끼 모티브: 일반 사용자는 거위 무리, 관리자는 해커 오리
const AVATARS = ['goose-relax', 'goose-write', 'goose-coffee', 'goose-read', 'goose-wave', 'goose-walk'];

/**
 * 프로필 그림. 관리자는 해커 오리, 일반 사용자는 거위 그림 중 하나.
 * 사용자 ID로 골라서 같은 사람은 항상 같은 거위가 나온다.
 */
function avatarOf(user) {
  if (user.role === 'ADMIN') {
    return '/images/hacker_duck.png';
  }
  return `/images/avatars/${AVATARS[user.id % AVATARS.length]}.svg`;
}

/** 로그인이 필요한 페이지에서 호출. 로그인하지 않았으면 로그인 페이지로 이동. */
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

/** 숫자를 로마 숫자로 변환 (1~3999). 범위를 벗어나면 그대로 반환. 예: 9 → IX */
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

/** 생성 직후에도 수정 시각이 미세하게 다르므로 1초 넘게 차이 날 때만 수정된 것으로 본다. */
function isEdited(item) {
  return new Date(item.updatedAt) - new Date(item.createdAt) > 1000;
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
