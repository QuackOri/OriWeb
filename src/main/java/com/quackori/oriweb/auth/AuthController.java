package com.quackori.oriweb.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quackori.oriweb.auth.dto.LoginRequest;
import com.quackori.oriweb.auth.dto.SignupRequest;
import com.quackori.oriweb.auth.dto.UserResponse;
import com.quackori.oriweb.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Tag(name = "Auth", description = "회원가입 / 로그인 / 로그아웃 (세션 인증)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final String githubClientId;

	public AuthController(AuthService authService,
			@Value("${spring.security.oauth2.client.registration.github.client-id}") String githubClientId) {
		this.authService = authService;
		this.githubClientId = githubClientId;
	}

	@Operation(summary = "회원가입")
	@PostMapping("/signup")
	public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
		User user = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
	}

	@Operation(summary = "로그인", description = "성공 시 세션을 생성하고 JSESSIONID 쿠키를 발급합니다.")
	@PostMapping("/login")
	public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		User user = authService.login(request);

		// Discard the old session and issue a new one (session fixation protection)
		HttpSession oldSession = httpRequest.getSession(false);
		if (oldSession != null) {
			oldSession.invalidate();
		}
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(SessionConst.LOGIN_USER_ID, user.getId());

		return UserResponse.from(user);
	}

	@Operation(summary = "로그아웃", description = "세션을 무효화합니다.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "OAuth 로그인 사용 가능 여부",
			description = "GitHub OAuth App이 설정되어 있으면 github: true. 로그인은 브라우저에서 /oauth2/authorization/github 로 이동합니다.")
	@GetMapping("/oauth-providers")
	public Map<String, Boolean> oauthProviders() {
		return Map.of("github", !"unset".equals(githubClientId));
	}

	@Operation(summary = "내 정보 조회", description = "현재 세션의 로그인 사용자를 반환합니다. 로그인하지 않았으면 401.")
	@GetMapping("/me")
	public UserResponse me(HttpSession session) {
		return UserResponse.from(authService.getLoginUser(session));
	}

}
