package com.quackori.oriweb.auth;

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

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public User signup(SignupRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
		}
		User user = new User(request.username(), passwordEncoder.encode(request.password()));
		return userRepository.save(user);
	}

	public User login(LoginRequest request) {
		User user = userRepository.findByUsername(request.username())
				.filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));
		if (user.isSuspended()) {
			throw new ApiException(HttpStatus.FORBIDDEN, SUSPENDED_MESSAGE);
		}
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
