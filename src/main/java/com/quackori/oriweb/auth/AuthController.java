package com.quackori.oriweb.auth;

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
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "회원가입 / 로그인 / 로그아웃 (세션 인증)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

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

		// 기존 세션을 버리고 새 세션 발급 (세션 고정 방지)
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

	@Operation(summary = "내 정보 조회", description = "현재 세션의 로그인 사용자를 반환합니다. 로그인하지 않았으면 401.")
	@GetMapping("/me")
	public UserResponse me(HttpSession session) {
		return UserResponse.from(authService.getLoginUser(session));
	}

}
