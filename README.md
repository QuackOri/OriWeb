# OriWeb

> ⚠️ **교육용 의도적 취약 애플리케이션입니다.**
> 모의해킹 실습을 위해 보안 취약점을 포함하고 있습니다.
> 반드시 **로컬 환경에서만** 실행하고, 공개 서버나 인터넷에 노출된 환경에 배포하지 마세요.

세션 인증, 게시글(파일 업로드/다운로드 포함), 댓글 기능을 가진 간단한 게시판입니다.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 (Spring Web, Spring Data JPA, Validation) |
| Build | Gradle 8.14 (Gradle Wrapper 포함) |
| Database | MySQL 8.4 |
| Frontend | HTML, CSS, JavaScript (fetch API) |
| API 문서 | Swagger UI (springdoc-openapi) |
| 실행 환경 | Docker, Docker Compose |

## 실행 방법 (Docker Compose)

### 1. 사전 준비

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) 설치 및 실행
- Git

Java, Gradle, MySQL은 설치하지 않아도 됩니다. 빌드와 DB 모두 Docker 컨테이너 안에서 실행됩니다.

### 2. 소스 받기

```bash
git clone https://github.com/QuackOri/OriWeb.git
cd OriWeb
```

### 3. 실행

```bash
docker compose up --build -d
```

처음 실행하면 이미지 다운로드와 빌드 때문에 몇 분 정도 걸립니다.
MySQL이 준비된 뒤 애플리케이션이 시작됩니다.

### 4. 접속

| 주소 | 설명 |
|---|---|
| http://localhost:8080 | 게시판 |
| http://localhost:8080/swagger-ui.html | Swagger UI (API 문서, 테스트) |
| http://localhost:8080/api/health | 서버 상태 확인 (`{"status":"UP"}`) |

처음에는 데이터가 없으므로 회원가입 후 사용하세요.

### 5. 종료 및 관리

```bash
# 로그 확인
docker compose logs -f app

# 종료 (DB 데이터와 업로드 파일은 유지)
docker compose down

# 종료 + DB 데이터와 업로드 파일까지 모두 삭제 (초기화)
docker compose down -v
```

### 포트와 계정 정보

| 서비스 | 컨테이너 | 포트 | 비고 |
|---|---|---|---|
| 애플리케이션 | `oriweb-app` | `127.0.0.1:8080` | |
| MySQL | `oriweb-db` | `127.0.0.1:3306` | DB `oriweb` / 계정 `oriweb` / 비밀번호 `oriweb` |

- 모든 포트는 `127.0.0.1`에만 바인딩되어 같은 PC에서만 접속할 수 있습니다.
- DB 계정은 로컬 실습용 값입니다. (`docker-compose.yml`)
- 8080 또는 3306 포트를 이미 사용 중이면 `docker-compose.yml`의 `ports` 왼쪽 값을 바꿔서 실행하세요. (예: `127.0.0.1:8081:8080`)

## 주요 기능

| 구분 | 기능 |
|---|---|
| 사용자 인증 | 회원가입, 로그인, 로그아웃 (HttpSession 기반 세션 인증, `JSESSIONID` 쿠키) |
| 게시글 | 목록(페이징), 상세, 작성, 수정, 삭제 |
| 첨부파일 | 게시글에 여러 파일 업로드(파일당 최대 10MB), 원본 파일명으로 다운로드, 삭제 |
| 댓글 | 작성, 수정, 삭제 |

- 게시글 작성, 댓글 작성, 파일 업로드는 로그인이 필요합니다.
- 게시글/첨부파일 수정·삭제는 게시글 작성자만, 댓글 수정·삭제는 댓글 작성자만 가능합니다.
- 게시글을 삭제하면 댓글과 첨부파일도 함께 삭제됩니다.

## API

자세한 요청/응답 형식은 Swagger UI(http://localhost:8080/swagger-ui.html)에서 확인할 수 있습니다.

| 구분 | 메서드 | URL | 설명 | 권한 |
|---|---|---|---|---|
| Auth | POST | `/api/auth/signup` | 회원가입 | - |
| | POST | `/api/auth/login` | 로그인 (세션 생성) | - |
| | POST | `/api/auth/logout` | 로그아웃 (세션 삭제) | - |
| | GET | `/api/auth/me` | 로그인 사용자 조회 | 로그인 |
| Post | GET | `/api/posts?page=0&size=10` | 게시글 목록 (최신순) | - |
| | GET | `/api/posts/{id}` | 게시글 상세 (첨부파일 목록 포함) | - |
| | POST | `/api/posts` | 게시글 작성 | 로그인 |
| | PUT | `/api/posts/{id}` | 게시글 수정 | 작성자 |
| | DELETE | `/api/posts/{id}` | 게시글 삭제 | 작성자 |
| Attachment | POST | `/api/posts/{postId}/attachments` | 첨부파일 업로드 (multipart, `files`) | 게시글 작성자 |
| | GET | `/api/posts/{postId}/attachments` | 첨부파일 목록 | - |
| | GET | `/api/attachments/{id}/download` | 첨부파일 다운로드 | - |
| | DELETE | `/api/attachments/{id}` | 첨부파일 삭제 | 게시글 작성자 |
| Comment | GET | `/api/posts/{postId}/comments` | 댓글 목록 | - |
| | POST | `/api/posts/{postId}/comments` | 댓글 작성 | 로그인 |
| | PUT | `/api/comments/{id}` | 댓글 수정 | 댓글 작성자 |
| | DELETE | `/api/comments/{id}` | 댓글 삭제 | 댓글 작성자 |

오류 응답은 다음 형식으로 통일되어 있습니다.

```json
{ "status": 403, "message": "게시글 작성자만 가능합니다." }
```

## 화면

| 페이지 | 주소 |
|---|---|
| 게시글 목록 | `/` |
| 로그인 | `/login.html` |
| 회원가입 | `/signup.html` |
| 게시글 상세 (첨부파일, 댓글) | `/post.html?id={id}` |
| 게시글 작성 / 수정 | `/write.html`, `/write.html?id={id}` |

## 데이터베이스

테이블은 애플리케이션 시작 시 JPA가 자동으로 생성합니다. (`ddl-auto: update`)

| 테이블 | 주요 컬럼 |
|---|---|
| `users` | id, username, password(BCrypt), created_at |
| `posts` | id, title, content, author_id → users, created_at, updated_at |
| `comments` | id, post_id → posts, author_id → users, content, created_at, updated_at |
| `attachments` | id, post_id → posts, original_name, stored_name(UUID), size, content_type, created_at |

첨부파일은 업로드 폴더(Docker: `/app/uploads`, `upload-data` 볼륨)에 UUID 이름으로 저장되고, 원본 파일명은 DB에 저장됩니다.

## 프로젝트 구조

```
src/main/java/com/quackori/oriweb
├─ auth/         # 회원가입, 로그인, 로그아웃, 세션
├─ user/         # 사용자 엔티티
├─ post/         # 게시글
├─ attachment/   # 첨부파일 업로드/다운로드, 파일 저장소
├─ comment/      # 댓글
└─ common/       # 공통 예외 처리, 페이지 응답
src/main/resources
├─ application.yml
└─ static/       # 화면 (HTML, CSS, JS)
```

## 로컬 개발 (선택)

IntelliJ 등에서 애플리케이션을 직접 실행하려면 JDK 21이 필요합니다.

```bash
# DB만 Docker로 실행
docker compose up -d db

# 애플리케이션 실행 (Windows는 gradlew.bat)
./gradlew bootRun
```

로컬 실행 시 업로드 파일은 프로젝트 폴더의 `uploads/`에 저장됩니다.
