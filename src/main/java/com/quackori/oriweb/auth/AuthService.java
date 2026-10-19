package com.quackori.oriweb.auth;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quackori.oriweb.auth.dto.LoginRequest;
import com.quackori.oriweb.auth.dto.SignupRequest;
import com.quackori.oriweb.common.ApiException;
import com.quackori.oriweb.user.User;
import com.quackori.oriweb.user.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	public static final String SUSPENDED_MESSAGE = "정지된 계정입니다. 관리자에게 문의하세요.";

	/** 로그인 실패 임계치 (ISMS-P 참고 기준). 도달 시 계정을 일정 시간 잠근다. */
	public static final int MAX_FAILED_ATTEMPTS = 5;
	private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public User signup(SignupRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
		}
		validatePasswordNotGuessable(request.username(), request.password());
		User user = new User(request.username(), passwordEncoder.encode(request.password()));
		return userRepository.save(user);
	}

	/** Blocks easily guessable passwords: ones that contain the username, or 4+ sequential/repeated characters. */
	private void validatePasswordNotGuessable(String username, String password) {
		String lower = password.toLowerCase();
		if (lower.contains(username.toLowerCase())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "비밀번호에 아이디를 포함할 수 없습니다.");
		}
		if (hasRepeatedRun(lower) || hasSequentialRun(lower)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "연속되거나 반복되는 문자는 사용할 수 없습니다.");
		}
	}

	/** True if the same character repeats 4 or more times in a row (e.g. "aaaa"). */
	private static boolean hasRepeatedRun(String s) {
		int run = 1;
		for (int i = 1; i < s.length(); i++) {
			run = (s.charAt(i) == s.charAt(i - 1)) ? run + 1 : 1;
			if (run >= 4) {
				return true;
			}
		}
		return false;
	}

	/** True if 4 or more characters run in ascending or descending order (e.g. "1234", "dcba"). */
	private static boolean hasSequentialRun(String s) {
		int ascending = 1;
		int descending = 1;
		for (int i = 1; i < s.length(); i++) {
			int diff = s.charAt(i) - s.charAt(i - 1);
			ascending = (diff == 1) ? ascending + 1 : 1;
			descending = (diff == -1) ? descending + 1 : 1;
			if (ascending >= 4 || descending >= 4) {
				return true;
			}
		}
		return false;
	}

	@Transactional
	public User login(LoginRequest request) {
		User user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));
		if (user.isLoginLocked()) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
					"로그인 실패 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
		}
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			user.recordFailedLogin(MAX_FAILED_ATTEMPTS, LOCK_DURATION);
			throw new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
		}
		if (user.isSuspended()) {
			throw new ApiException(HttpStatus.FORBIDDEN, SUSPENDED_MESSAGE);
		}
		user.resetFailedLogin();
		return user;
	}

	/**
	 * Returns the logged-in user from the session. Throws 401 if not logged in.
	 * Shared by all features that require login (posts, comments, ...).
	 * If the account was suspended while logged in, invalidates the session and throws 403.
	 */
	public User getLoginUser(HttpSession session) {
		Object userId = session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}
		User user = userRepository.findById((Long) userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
		if (user.isSuspended()) {
			session.invalidate();
			throw new ApiException(HttpStatus.FORBIDDEN, SUSPENDED_MESSAGE);
		}
		return user;
	}

	/** Allows admins only. Throws 401 if not logged in, 403 if not an admin. */
	public User getAdmin(HttpSession session) {
		User user = getLoginUser(session);
		if (!user.isAdmin()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "관리자만 가능합니다.");
		}
		return user;
	}

}
