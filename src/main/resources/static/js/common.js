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
  const header = document.createElement('header');
  header.innerHTML = `
    <a class="logo" href="/">OriWeb</a>
    <nav></nav>`;
  const nav = header.querySelector('nav');

  if (me) {
    const name = document.createElement('span');
    name.textContent = `${me.username}님`;
    const logout = document.createElement('button');
    logout.className = 'link';
    logout.textContent = '로그아웃';
    logout.onclick = async () => {
      await api('POST', '/api/auth/logout');
      location.href = '/';
    };
    nav.append(name, logout);
  } else {
    nav.innerHTML = `<a href="/login.html">로그인</a><a href="/signup.html">회원가입</a>`;
  }

  document.body.prepend(header);
  return me;
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

/** 생성 직후에도 수정 시각이 미세하게 다르므로 1초 넘게 차이 날 때만 수정된 것으로 본다. */
function isEdited(item) {
  return new Date(item.updatedAt) - new Date(item.createdAt) > 1000;
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
