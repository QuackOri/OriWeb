package com.quackori.oriweb.auth.oauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quackori.oriweb.auth.AuthService;
import com.quackori.oriweb.auth.SessionConst;
import com.quackori.oriweb.auth.dto.UserResponse;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Signup confirmation for a GitHub account used for the first time.
 * The GitHub account info comes only from the server-side session set by OAuthLoginSuccessHandler.
 */
@Tag(name = "Auth", description = "회원가입 / 로그인 / 로그아웃 (세션 인증)")
@RestController
@RequestMapping("/api/auth/oauth-signup")
@RequiredArgsConstructor
public class OAuthSignupController {

	private final OAuthUserService oAuthUserService;

	@Operation(summary = "GitHub 가입 확인 정보", description = "GitHub 인증 후 가입 대기 중인 계정과 가입될 아이디를 반환합니다.")
	@GetMapping
	public OAuthSignupInfo getPending(HttpSession session) {
		PendingOAuthSignup pending = getPendingOrThrow(session);
		return new OAuthSignupInfo(pending.provider(), pending.login(), oAuthUserService.proposeUsername(pending));
	}

	@Operation(summary = "GitHub 계정으로 가입", description = "가입 대기 중인 GitHub 계정으로 회원가입하고 로그인합니다.")
	@PostMapping
	public ResponseEntity<UserResponse> signup(HttpServletRequest request) {
		HttpSession session = request.getSession();
		PendingOAuthSignup pending = getPendingOrThrow(session);

		User user = oAuthUserService.signup(pending);
		session.removeAttribute(SessionConst.OAUTH_PENDING_SIGNUP);
		if (user.isSuspended()) {
			throw new ApiException(HttpStatus.FORBIDDEN, AuthService.SUSPENDED_MESSAGE);
		}

		// Issue a new session ID on login (session fixation protection)
		request.changeSessionId();
		session.setAttribute(SessionConst.LOGIN_USER_ID, user.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
	}

	@Operation(summary = "GitHub 가입 취소", description = "가입 대기 정보를 삭제합니다. 계정은 만들어지지 않습니다.")
	@DeleteMapping
	public ResponseEntity<Void> cancel(HttpSession session) {
		session.removeAttribute(SessionConst.OAUTH_PENDING_SIGNUP);
		return ResponseEntity.noContent().build();
	}

	private PendingOAuthSignup getPendingOrThrow(HttpSession session) {
		Object pending = session.getAttribute(SessionConst.OAUTH_PENDING_SIGNUP);
		if (!(pending instanceof PendingOAuthSignup signup)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "가입 대기 중인 GitHub 계정이 없습니다. GitHub 인증을 다시 진행해주세요.");
		}
		return signup;
	}

}
