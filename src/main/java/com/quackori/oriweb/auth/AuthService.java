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
		return userRepository.findByUsername(request.username())
				.filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));
	}

	/**
	 * 세션에서 로그인 사용자를 조회한다. 로그인하지 않았으면 401 예외.
	 * 게시글, 댓글 등 로그인이 필요한 기능에서 공통으로 사용한다.
	 */
	public User getLoginUser(HttpSession session) {
		Object userId = session.getAttribute(SessionConst.LOGIN_USER_ID);
		if (userId == null) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}
		return userRepository.findById((Long) userId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
	}

}
